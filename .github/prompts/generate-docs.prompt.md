You are a Java development and code analysis expert. Please write detailed **User Guide** and **Developer Guide** documentation with the following requirements:

- All content must be in Markdown format, stored under the `docs/` directory (relative to the project root). Bilingual content (Chinese and English) is required, with languages stored independently; **English is the default language**.
- The `docs/` directory (relative to the project root) must contain a `README.md` that serves as an entry point linking to both the User Guide and the Developer Guide. Each of the User Guide and Developer Guide must also have its own `README.md`.
- User Guide documents are stored under `docs/user-guide/` (relative to the project root); Developer Guide documents are stored under `docs/developer-guide/` (relative to the project root).
- Documentation must analyze **only** the `release` branch and the `release-1.x` branch (if it exists). Each branch has its own subdirectory under the `docs/` directory: the `release` branch corresponds to the `current/` directory, and the `release-1.x` branch corresponds to the `1.x/` directory.
- Independent Markdown documents must be written **per Maven module**.
- The User Guide must include: **API explanations**, **code examples** (referencing code from `test-cases` where possible), **version information**, and **version compatibility ranges**.
- The Developer Guide must include detailed explanations of: **architecture design**, **source code analysis**, **design patterns**, and **complex workflows** illustrated using **flowcharts**.
- **Module dependency relationships** must be illustrated using **flowcharts**.
- The root `README.md` must link to both the **User Guide** and the **Developer Guide**.

---

### 📁 Implied Directory Structure (for reference)

```
docs/
├── README.md                    # Root entry (EN, default)
├── README.zh.md                 # Root entry (ZH)
├── current/                     # release branch
│   ├── user-guide/
│   │   ├── README.md
│   │   ├── README.zh.md
│   │   ├── module-A.md
│   │   ├── module-A.zh.md
│   │   └── ...
│   └── developer-guide/
│       ├── README.md
│       ├── README.zh.md
│       ├── architecture.md
│       ├── architecture.zh.md
│       └── ...
└── 1.x/                         # release-1.x branch
    ├── user-guide/
    └── developer-guide/
```

---