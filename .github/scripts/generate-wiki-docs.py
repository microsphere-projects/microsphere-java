#!/usr/bin/env python3
"""
Microsphere Java Wiki Documentation Generator

Parses Java source files in the microsphere-java project and generates
Markdown wiki pages for each public Java component (class, interface, enum, annotation).

Each wiki page includes:
- Detailed explanation of the component
- Example code extracted from Javadoc
- Version compatibility information
- Since version metadata

Generated pages are written to a specified output directory, one page per component,
ready to be pushed to the GitHub wiki repository.
"""

import os
import re
import sys
import argparse
from collections import OrderedDict

# ──────────────────────────────────────────────
# Constants
# ──────────────────────────────────────────────

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Source directory path suffix
SRC_MAIN_JAVA = os.path.join("src", "main", "java")


def _discover_modules(project_root):
    """Discover module directories that contain Java sources."""
    modules = []
    for entry in sorted(os.listdir(project_root)):
        entry_path = os.path.join(project_root, entry)
        if os.path.isdir(entry_path) and os.path.isdir(os.path.join(entry_path, SRC_MAIN_JAVA)):
            modules.append(entry)
    return modules


def _read_java_versions(project_root):
    """Read Java versions from the CI workflow matrix configuration."""
    workflow_path = os.path.join(project_root, '.github', 'workflows', 'maven-build.yml')
    with open(workflow_path, 'r', encoding='utf-8') as f:
        content = f.read()
    match = re.search(r'matrix:\s*\n\s*java:\s*\[([^\]]+)\]', content)
    if match:
        return [v.strip().strip("'\"") for v in match.group(1).split(',')]
    print("WARNING: Could not parse Java versions from matrix in maven-build.yml", file=sys.stderr)
    return []


def _read_pom_revision(project_root):
    """Read the 'revision' property from the root pom.xml."""
    pom_path = os.path.join(project_root, 'pom.xml')
    with open(pom_path, 'r', encoding='utf-8') as f:
        content = f.read()
    match = re.search(r'<revision>([^<]+)</revision>', content)
    if match:
        return match.group(1).strip()
    print("WARNING: Could not find <revision> property in pom.xml", file=sys.stderr)
    return ""


def _read_pom_artifact_id(project_root):
    """Read the project artifactId from the root pom.xml (outside the <parent> block)."""
    pom_path = os.path.join(project_root, 'pom.xml')
    with open(pom_path, 'r', encoding='utf-8') as f:
        content = f.read()
    no_parent = re.sub(r'<parent>.*?</parent>', '', content, flags=re.DOTALL)
    match = re.search(r'<artifactId>([^<]+)</artifactId>', no_parent)
    if match:
        return match.group(1).strip()
    print("WARNING: Could not find <artifactId> in pom.xml", file=sys.stderr)
    return ""


def _read_readme_title(project_root):
    """Read the top-level heading from README.md."""
    readme_path = os.path.join(project_root, 'README.md')
    with open(readme_path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line.startswith('# '):
                return line[2:].strip()
    print("WARNING: Could not find a title heading in README.md", file=sys.stderr)
    return ""


MODULES = _discover_modules(PROJECT_ROOT)
JAVA_VERSIONS = _read_java_versions(PROJECT_ROOT)
PROJECT_VERSION = _read_pom_revision(PROJECT_ROOT)
ARTIFACT_ID = _read_pom_artifact_id(PROJECT_ROOT)
PROJECT_TITLE = _read_readme_title(PROJECT_ROOT)

# Regex patterns
CLASS_DECL_RE = re.compile(
    r'^(?:public\s+)?(?:abstract\s+)?(?:final\s+)?'
    r'(?:(?:class|interface|enum|@interface)\s+)'
    r'(\w+)'
    r'(?:<[^{]*>)?'
    r'(?:\s+extends\s+[\w.<>, ]+)?'
    r'(?:\s+implements\s+[\w.<>, ]+)?',
    re.MULTILINE,
)

PACKAGE_RE = re.compile(r'^\s*package\s+([\w.]+)\s*;', re.MULTILINE)

JAVADOC_BLOCK_RE = re.compile(r'/\*\*(.*?)\*/', re.DOTALL)

SINCE_TAG_RE = re.compile(r'@since\s+(.+?)(?:\n|\r|$)')
AUTHOR_TAG_RE = re.compile(r'@author\s+(.+?)(?:\n|\r|$)')
SEE_TAG_RE = re.compile(r'@see\s+(.+?)(?:\n|\r|$)')
PARAM_TAG_RE = re.compile(r'@param\s+(\S+)\s+(.*?)(?=@\w|\Z)', re.DOTALL)

# Matches @Since annotation on a class (not inside Javadoc)
SINCE_ANNOTATION_RE = re.compile(r'@Since\s*\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\']\s*\)')

# Code example blocks in Javadoc
CODE_EXAMPLE_RE = re.compile(r'<pre>\s*\{@code\s*(.*?)\}</pre>', re.DOTALL)
CODE_EXAMPLE_ALT_RE = re.compile(r'<pre>\s*(.*?)</pre>', re.DOTALL)

# HTML tags used in Javadoc
LINK_TAG_RE = re.compile(r'\{@link\s+([^}]+)\}')
CODE_TAG_RE = re.compile(r'\{@code\s+([^}]+)\}')
LINKPLAIN_TAG_RE = re.compile(r'\{@linkplain\s+([^}]+)\}')
VALUE_TAG_RE = re.compile(r'\{@value\s+([^}]+)\}')

# Method/field signatures
PUBLIC_METHOD_RE = re.compile(
    r'(?:/\*\*(.*?)\*/\s*)?'
    r'(?:@\w+(?:\([^)]*\))?\s*)*'
    r'(public\s+(?:static\s+)?(?:final\s+)?(?:synchronized\s+)?'
    r'(?:<[^>]+>\s+)?'
    r'\S+\s+'  # return type
    r'(\w+)\s*'  # method name
    r'\([^)]*\))',  # parameters
    re.DOTALL,
)


# ──────────────────────────────────────────────
# Javadoc Parsing Utilities
# ──────────────────────────────────────────────

def clean_javadoc_line(line):
    """Remove leading whitespace, asterisks, and extra spaces from a Javadoc line."""
    line = line.strip()
    if line.startswith('*'):
        line = line[1:]
        if line.startswith(' '):
            line = line[1:]
    return line


def parse_javadoc(javadoc_text):
    """Parse a Javadoc comment block into structured components."""
    if not javadoc_text:
        return {
            "description": "",
            "since": "",
            "author": "",
            "see": [],
            "params": [],
            "examples": [],
        }

    lines = javadoc_text.split('\n')
    cleaned_lines = [clean_javadoc_line(line) for line in lines]
    full_text = '\n'.join(cleaned_lines)

    # Extract tags
    since_match = SINCE_TAG_RE.search(full_text)
    since = since_match.group(1).strip() if since_match else ""

    author_match = AUTHOR_TAG_RE.search(full_text)
    author = author_match.group(1).strip() if author_match else ""
    # Clean HTML from author
    author = re.sub(r'<[^>]+>', '', author).strip()

    see_matches = SEE_TAG_RE.findall(full_text)
    see_refs = [s.strip() for s in see_matches]

    # Extract description (text before any @tag)
    desc_lines = []
    for line in cleaned_lines:
        stripped = line.strip()
        if stripped.startswith('@'):
            break
        desc_lines.append(line)
    description = '\n'.join(desc_lines).strip()

    # Extract code examples
    examples = []
    for match in CODE_EXAMPLE_RE.finditer(javadoc_text):
        code = match.group(1).strip()
        # Clean Javadoc asterisks from code lines
        code_lines = code.split('\n')
        cleaned_code = '\n'.join(clean_javadoc_line(l) for l in code_lines)
        examples.append(cleaned_code.strip())

    if not examples:
        for match in CODE_EXAMPLE_ALT_RE.finditer(javadoc_text):
            code = match.group(1).strip()
            if '{@code' not in code and len(code) > 10:
                code_lines = code.split('\n')
                cleaned_code = '\n'.join(clean_javadoc_line(l) for l in code_lines)
                examples.append(cleaned_code.strip())

    return {
        "description": description,
        "since": since,
        "author": author,
        "see": see_refs,
        "params": [],
        "examples": examples,
    }


def convert_javadoc_to_markdown(text):
    """Convert Javadoc HTML/tags to Markdown."""
    if not text:
        return ""

    # Convert {@link ...} to `...`
    text = LINK_TAG_RE.sub(r'`\1`', text)
    text = LINKPLAIN_TAG_RE.sub(r'`\1`', text)
    text = CODE_TAG_RE.sub(r'`\1`', text)
    text = VALUE_TAG_RE.sub(r'`\1`', text)

    # Convert basic HTML
    text = re.sub(r'<p\s*/?>', '\n\n', text)
    text = re.sub(r'</p>', '', text)
    text = re.sub(r'<br\s*/?>', '\n', text)
    text = re.sub(r'<b>(.*?)</b>', r'**\1**', text)
    text = re.sub(r'<i>(.*?)</i>', r'*\1*', text)
    text = re.sub(r'<em>(.*?)</em>', r'*\1*', text)
    text = re.sub(r'<strong>(.*?)</strong>', r'**\1**', text)
    text = re.sub(r'<code>(.*?)</code>', r'`\1`', text)
    text = re.sub(r'<h3>(.*?)</h3>', r'### \1', text)
    text = re.sub(r'<h4>(.*?)</h4>', r'#### \1', text)
    text = re.sub(r'<ul>', '', text)
    text = re.sub(r'</ul>', '', text)
    text = re.sub(r'<li>(.*?)</li>', r'- \1', text, flags=re.DOTALL)
    text = re.sub(r'<li>', '- ', text)

    # Remove remaining HTML tags (except <pre>)
    text = re.sub(r'<(?!pre|/pre)[^>]+>', '', text)

    return text.strip()


# ──────────────────────────────────────────────
# Java Source File Parser
# ──────────────────────────────────────────────

class JavaComponent:
    """Represents a parsed Java component (class, interface, enum, annotation)."""

    def __init__(self):
        self.name = ""
        self.package = ""
        self.module = ""
        self.component_type = ""  # class, interface, enum, annotation
        self.description = ""
        self.since_version = ""
        self.author = ""
        self.see_refs = []
        self.examples = []
        self.extends_class = ""
        self.implements_interfaces = []
        self.declaration_line = ""
        self.public_methods = []
        self.source_path = ""

    @property
    def fully_qualified_name(self):
        if self.package:
            return f"{self.package}.{self.name}"
        return self.name

    @property
    def wiki_page_name(self):
        """Generate a wiki-friendly page name."""
        return self.fully_qualified_name.replace('.', '-')


class JavaMethod:
    """Represents a parsed public method."""

    def __init__(self):
        self.name = ""
        self.signature = ""
        self.description = ""
        self.since_version = ""
        self.examples = []
        self.params = []


def parse_java_file(filepath, module_name):
    """Parse a Java source file and extract component information."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except (IOError, UnicodeDecodeError):
        return None

    # Extract package
    pkg_match = PACKAGE_RE.search(content)
    package_name = pkg_match.group(1) if pkg_match else ""

    # Check for package-info.java
    if os.path.basename(filepath) == 'package-info.java':
        return None

    # Find the class/interface/enum/annotation declaration
    # First, find the Javadoc that precedes the class declaration
    class_javadoc = None
    class_decl_match = None

    # Strategy: find all Javadoc blocks and the class declaration
    javadoc_blocks = list(JAVADOC_BLOCK_RE.finditer(content))

    # Find the main type declaration
    for line in content.split('\n'):
        stripped = line.strip()
        if re.match(r'(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|@interface)\s+', stripped):
            class_decl_match = stripped
            break

    if not class_decl_match:
        return None

    # Determine component type (check @interface before interface)
    comp_type = "class"
    if re.search(r'@interface\s+', class_decl_match):
        comp_type = "annotation"
    elif re.search(r'\binterface\s+', class_decl_match):
        comp_type = "interface"
    elif re.search(r'\benum\s+', class_decl_match):
        comp_type = "enum"

    # Extract class name
    name_match = re.search(
        r'(?:class|interface|enum|@interface)\s+(\w+)', class_decl_match
    )
    if not name_match:
        return None

    class_name = name_match.group(1)

    # Skip non-public classes, inner classes, and module-info
    if 'public' not in class_decl_match and comp_type != "annotation":
        # Check if the file name matches the class name (top-level class)
        file_basename = os.path.splitext(os.path.basename(filepath))[0]
        if file_basename != class_name:
            return None

    if class_name in ('module-info', 'package-info'):
        return None

    # Find the class-level Javadoc (the last Javadoc before the class declaration)
    class_decl_pos = content.find(class_decl_match)
    for jd_block in reversed(javadoc_blocks):
        if jd_block.end() <= class_decl_pos:
            # Verify there's no other declaration between this Javadoc and the class
            between = content[jd_block.end():class_decl_pos].strip()
            # Remove annotations between Javadoc and class decl
            between_cleaned = re.sub(r'@\w+(?:\([^)]*\))?', '', between).strip()
            if not between_cleaned or between_cleaned.startswith('@'):
                class_javadoc = jd_block.group(1)
                break

    # Parse the class Javadoc
    javadoc_info = parse_javadoc(class_javadoc)

    # Check for @Since annotation on the class (limit search to nearby context)
    search_start = max(0, class_decl_pos - 2000)
    since_annotation = SINCE_ANNOTATION_RE.search(content[search_start:class_decl_pos + len(class_decl_match)])
    annotation_since = since_annotation.group(1) if since_annotation else ""

    # Extract extends/implements
    extends_match = re.search(r'\bextends\s+([\w.<>, ]+?)(?:\s+implements|\s*\{)', class_decl_match)
    extends_class = extends_match.group(1).strip() if extends_match else ""

    implements_match = re.search(r'\bimplements\s+([\w.<>, ]+?)(?:\s*\{|$)', class_decl_match)
    implements_interfaces = []
    if implements_match:
        impl_str = implements_match.group(1).strip().rstrip('{').strip()
        implements_interfaces = [i.strip() for i in impl_str.split(',') if i.strip()]

    # Parse public methods
    public_methods = extract_public_methods(content, class_decl_pos)

    # Build the component
    component = JavaComponent()
    component.name = class_name
    component.package = package_name
    component.module = module_name
    component.component_type = comp_type
    component.description = javadoc_info["description"]
    component.since_version = javadoc_info["since"] or annotation_since
    component.author = javadoc_info["author"]
    component.see_refs = javadoc_info["see"]
    component.examples = javadoc_info["examples"]
    component.extends_class = extends_class
    component.implements_interfaces = implements_interfaces
    component.declaration_line = class_decl_match.rstrip('{').strip()
    component.public_methods = public_methods
    component.source_path = os.path.relpath(filepath, PROJECT_ROOT)

    return component


def extract_public_methods(content, class_start_pos):
    """Extract public methods from the class body."""
    methods = []
    # Only look at content after class declaration
    body = content[class_start_pos:]

    for match in PUBLIC_METHOD_RE.finditer(body):
        javadoc_text = match.group(1)
        full_signature = match.group(2)
        method_name = match.group(3)

        # Skip constructors, getters/setters that are trivial
        if method_name in ('toString', 'hashCode', 'equals', 'clone'):
            continue

        method = JavaMethod()
        method.name = method_name
        method.signature = full_signature.strip()

        if javadoc_text:
            method_jd = parse_javadoc(javadoc_text)
            method.description = method_jd["description"]
            method.since_version = method_jd["since"]
            method.examples = method_jd["examples"]

        methods.append(method)

    return methods[:20]  # Limit to 20 methods per class to keep docs manageable


# ──────────────────────────────────────────────
# Wiki Page Generator
# ──────────────────────────────────────────────

def generate_wiki_page(component):
    """Generate a Markdown wiki page for a Java component."""
    lines = []

    # Title
    type_label = component.component_type.capitalize()
    lines.append(f"# {component.name}")
    lines.append("")

    # Metadata badge line
    badges = []
    badges.append(f"**Type:** `{type_label}`")
    badges.append(f"**Module:** `{component.module}`")
    badges.append(f"**Package:** `{component.package}`")
    if component.since_version:
        badges.append(f"**Since:** `{component.since_version}`")
    lines.append(" | ".join(badges))
    lines.append("")

    # Source link
    lines.append(f"> **Source:** [`{component.source_path}`]"
                 f"(https://github.com/microsphere-projects/{ARTIFACT_ID}/blob/main/{component.source_path})")
    lines.append("")

    # ── Overview ──
    lines.append("## Overview")
    lines.append("")
    if component.description:
        desc_md = convert_javadoc_to_markdown(component.description)
        lines.append(desc_md)
    else:
        lines.append(f"`{component.name}` is a {type_label.lower()} in the "
                     f"`{component.package}` package of the `{component.module}` module.")
    lines.append("")

    # Declaration
    lines.append("### Declaration")
    lines.append("")
    lines.append("```java")
    lines.append(component.declaration_line)
    lines.append("```")
    lines.append("")

    # ── Author ──
    if component.author:
        lines.append(f"**Author:** {component.author}")
        lines.append("")

    # ── Since / Version Info ──
    lines.append("## Version Information")
    lines.append("")
    if component.since_version:
        lines.append(f"- **Introduced in:** `{component.since_version}`")
    else:
        lines.append(f"- **Introduced in:** `{PROJECT_VERSION}` (current)")
    lines.append(f"- **Current Project Version:** `{PROJECT_VERSION}`")
    lines.append("")

    # ── Version Compatibility ──
    lines.append("## Version Compatibility")
    lines.append("")
    lines.append("This component is tested and compatible with the following Java versions:")
    lines.append("")
    lines.append("| Java Version | Status |")
    lines.append("|:---:|:---:|")
    for v in JAVA_VERSIONS:
        lines.append(f"| Java {v} | ✅ Compatible |")
    lines.append("")

    # ── Examples ──
    has_examples = bool(component.examples)
    if not has_examples:
        # Check methods for examples
        for method in component.public_methods:
            if method.examples:
                has_examples = True
                break

    if has_examples:
        lines.append("## Examples")
        lines.append("")

        if component.examples:
            for i, example in enumerate(component.examples, 1):
                if len(component.examples) > 1:
                    lines.append(f"### Example {i}")
                    lines.append("")
                lines.append("```java")
                lines.append(example)
                lines.append("```")
                lines.append("")

        # Method-level examples
        method_examples_added = False
        for method in component.public_methods:
            if method.examples:
                if not method_examples_added:
                    lines.append(f"### Method Examples")
                    lines.append("")
                    method_examples_added = True
                lines.append(f"#### `{method.name}`")
                lines.append("")
                for example in method.examples:
                    lines.append("```java")
                    lines.append(example)
                    lines.append("```")
                    lines.append("")

    # ── Usage Guide ──
    lines.append("## Usage")
    lines.append("")
    lines.append("### Maven Dependency")
    lines.append("")
    lines.append("Add the following dependency to your `pom.xml`:")
    lines.append("")
    lines.append("```xml")
    lines.append("<dependency>")
    lines.append("    <groupId>io.github.microsphere-projects</groupId>")
    lines.append(f"    <artifactId>{component.module}</artifactId>")
    lines.append(f"    <version>${{{ARTIFACT_ID}.version}}</version>")
    lines.append("</dependency>")
    lines.append("```")
    lines.append("")
    lines.append(f"> **Tip:** Use the BOM (`{ARTIFACT_ID}-dependencies`) for consistent version management. "
                 f"See the [Getting Started](https://github.com/microsphere-projects/{ARTIFACT_ID}#getting-started) guide.")
    lines.append("")

    # ── Import ──
    lines.append("### Import")
    lines.append("")
    lines.append("```java")
    lines.append(f"import {component.fully_qualified_name};")
    lines.append("```")
    lines.append("")

    # ── Public API ──
    if component.public_methods:
        lines.append("## API Reference")
        lines.append("")
        lines.append("### Public Methods")
        lines.append("")
        lines.append("| Method | Description |")
        lines.append("|--------|-------------|")
        for method in component.public_methods:
            desc = method.description.split('\n')[0] if method.description else ""
            desc = convert_javadoc_to_markdown(desc)
            # Truncate long descriptions for the table
            if len(desc) > 120:
                desc = desc[:117] + "..."
            sig = method.signature.replace('|', '\\|')
            lines.append(f"| `{method.name}` | {desc} |")
        lines.append("")

        # Detailed method descriptions
        has_detailed_methods = any(
            m.description and len(m.description) > 50 for m in component.public_methods
        )
        if has_detailed_methods:
            lines.append("### Method Details")
            lines.append("")
            for method in component.public_methods:
                if method.description and len(method.description) > 50:
                    lines.append(f"#### `{method.name}`")
                    lines.append("")
                    lines.append(f"```java")
                    lines.append(method.signature)
                    lines.append("```")
                    lines.append("")
                    desc_md = convert_javadoc_to_markdown(method.description)
                    lines.append(desc_md)
                    lines.append("")
                    if method.since_version:
                        lines.append(f"*Since: {method.since_version}*")
                        lines.append("")

    # ── See Also ──
    if component.see_refs:
        lines.append("## See Also")
        lines.append("")
        for ref in component.see_refs:
            ref_clean = ref.strip()
            if ref_clean:
                lines.append(f"- `{ref_clean}`")
        lines.append("")

    # ── Footer ──
    lines.append("---")
    lines.append("")
    lines.append(f"*This documentation was auto-generated from the source code of "
                 f"[{ARTIFACT_ID}](https://github.com/microsphere-projects/{ARTIFACT_ID}).*")
    lines.append("")

    return '\n'.join(lines)


def generate_home_page(components_by_module):
    """Generate the Home (index) wiki page."""
    lines = []
    lines.append(f"# {PROJECT_TITLE} - API Documentation")
    lines.append("")
    lines.append(f"Welcome to the **{PROJECT_TITLE}** wiki! This documentation is auto-generated "
                 f"from the project source code and provides detailed information about each Java component.")
    lines.append("")
    lines.append("## Project Information")
    lines.append("")
    lines.append(f"- **Current Version:** `{PROJECT_VERSION}`")
    lines.append(f"- **Java Compatibility:** {', '.join('Java ' + v for v in JAVA_VERSIONS)}")
    lines.append("- **License:** Apache License 2.0")
    lines.append(f"- **Repository:** [microsphere-projects/{ARTIFACT_ID}]"
                 f"(https://github.com/microsphere-projects/{ARTIFACT_ID})")
    lines.append("")

    # Table of Contents by module
    lines.append("## Modules")
    lines.append("")

    for module_name, components in components_by_module.items():
        lines.append(f"### {module_name}")
        lines.append("")

        # Group by package
        by_package = OrderedDict()
        for comp in components:
            pkg = comp.package or "(default)"
            if pkg not in by_package:
                by_package[pkg] = []
            by_package[pkg].append(comp)

        for pkg, comps in by_package.items():
            lines.append(f"**`{pkg}`**")
            lines.append("")
            for comp in sorted(comps, key=lambda c: c.name):
                type_icon = {
                    "class": "📦",
                    "interface": "🔌",
                    "enum": "🔢",
                    "annotation": "🏷️",
                }.get(comp.component_type, "📄")
                wiki_link = comp.wiki_page_name
                lines.append(f"- {type_icon} [{comp.name}]({wiki_link}) - "
                             f"{comp.component_type.capitalize()}"
                             f"{' - Since ' + comp.since_version if comp.since_version else ''}")
            lines.append("")

    # Quick links
    lines.append("## Quick Links")
    lines.append("")
    lines.append(f"- [Getting Started](https://github.com/microsphere-projects/{ARTIFACT_ID}#getting-started)")
    lines.append(f"- [Building from Source](https://github.com/microsphere-projects/{ARTIFACT_ID}#building-from-source)")
    lines.append(f"- [Contributing](https://github.com/microsphere-projects/{ARTIFACT_ID}#contributing)")
    lines.append("- [JavaDoc](https://javadoc.io/doc/io.github.microsphere-projects)")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append(f"*This wiki is auto-generated from the source code of "
                 f"[{ARTIFACT_ID}](https://github.com/microsphere-projects/{ARTIFACT_ID}). "
                 f"To update, trigger the `wiki-publish` workflow.*")
    lines.append("")

    return '\n'.join(lines)


def generate_sidebar(components_by_module):
    """Generate the _Sidebar wiki page for navigation."""
    lines = []
    lines.append("**[Home](Home)**")
    lines.append("")

    for module_name, components in components_by_module.items():
        # Shorten module name for sidebar
        short_name = module_name.replace("microsphere-", "")
        lines.append(f"**{short_name}**")
        lines.append("")
        for comp in sorted(components, key=lambda c: c.name):
            wiki_link = comp.wiki_page_name
            lines.append(f"- [{comp.name}]({wiki_link})")
        lines.append("")

    return '\n'.join(lines)


# ──────────────────────────────────────────────
# Main
# ──────────────────────────────────────────────

def discover_java_files(project_root, modules):
    """Discover all main Java source files in the given modules."""
    java_files = []
    for module in modules:
        src_dir = os.path.join(project_root, module, SRC_MAIN_JAVA)
        if not os.path.isdir(src_dir):
            continue
        for root, _dirs, files in os.walk(src_dir):
            for fname in files:
                if fname.endswith('.java') and fname != 'package-info.java' and fname != 'module-info.java':
                    java_files.append((os.path.join(root, fname), module))
    return java_files


def main():
    parser = argparse.ArgumentParser(description=f"Generate wiki documentation for {ARTIFACT_ID}")
    parser.add_argument(
        "--output", "-o",
        default=os.path.join(PROJECT_ROOT, "wiki"),
        help="Output directory for generated wiki pages (default: <project>/wiki)",
    )
    parser.add_argument(
        "--project-root",
        default=PROJECT_ROOT,
        help=f"Root directory of the {ARTIFACT_ID} project",
    )
    args = parser.parse_args()

    project_root = args.project_root
    output_dir = args.output

    print(f"{PROJECT_TITLE} Wiki Documentation Generator")
    print(f"  Project root: {project_root}")
    print(f"  Output dir:   {output_dir}")
    print()

    # Discover Java files
    java_files = discover_java_files(project_root, MODULES)
    print(f"Found {len(java_files)} Java source files across {len(MODULES)} modules")
    print()

    # Parse all files
    components = []
    for filepath, module_name in java_files:
        component = parse_java_file(filepath, module_name)
        if component:
            components.append(component)

    print(f"Parsed {len(components)} Java components")
    print()

    # Group by module
    components_by_module = OrderedDict()
    for module in MODULES:
        module_components = [c for c in components if c.module == module]
        if module_components:
            components_by_module[module] = sorted(module_components, key=lambda c: (c.package, c.name))

    # Create output directory
    os.makedirs(output_dir, exist_ok=True)

    # Generate individual wiki pages
    page_count = 0
    for module_name, module_components in components_by_module.items():
        for comp in module_components:
            page_content = generate_wiki_page(comp)
            page_filename = f"{comp.wiki_page_name}.md"
            page_path = os.path.join(output_dir, page_filename)
            with open(page_path, 'w', encoding='utf-8') as f:
                f.write(page_content)
            page_count += 1

    print(f"Generated {page_count} wiki pages")

    # Generate Home page
    home_content = generate_home_page(components_by_module)
    with open(os.path.join(output_dir, "Home.md"), 'w', encoding='utf-8') as f:
        f.write(home_content)
    print("Generated Home.md")

    # Generate Sidebar
    sidebar_content = generate_sidebar(components_by_module)
    with open(os.path.join(output_dir, "_Sidebar.md"), 'w', encoding='utf-8') as f:
        f.write(sidebar_content)
    print("Generated _Sidebar.md")

    print()
    print(f"Wiki documentation generated successfully in: {output_dir}")
    print(f"Total pages: {page_count + 2} ({page_count} components + Home + Sidebar)")

    return 0


if __name__ == "__main__":
    sys.exit(main())
