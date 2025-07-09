/**
 *
 */
package io.microsphere.filter;

import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.util.ClassUtils.resolvePackageName;
import static io.microsphere.util.StringUtils.isBlank;

/**
 * {@link PackageNameClassNameFilter} is a {@link Filter} implementation that filters class names based on their package name.
 * <p>
 * This filter can be configured to match either exact package names or include sub-packages.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <ul>
 *     <li>To filter classes exactly in the package "io.microsphere":<br>
 *         {@code new PackageNameClassNameFilter("io.microsphere", false)}
 *     </li>
 *     <li>To filter classes in the package "io.microsphere" and its sub-packages:<br>
 *         {@code new PackageNameClassNameFilter("io.microsphere", true)}
 *     </li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PackageNameClassNameFilter
 * @since 1.0.0
 */
public class PackageNameClassNameFilter implements Filter<String> {

    private final String packageName;
    private final boolean includedSubPackages;
    private final String subPackageNamePrefix;

    /**
     * Constructor
     *
     * @param packageName         the name of package
     * @param includedSubPackages included sub-packages
     */
    public PackageNameClassNameFilter(String packageName, boolean includedSubPackages) {
        this.packageName = packageName;
        this.includedSubPackages = includedSubPackages;
        this.subPackageNamePrefix = includedSubPackages ? packageName + DOT_CHAR : null;
    }

    @Override
    public boolean accept(String className) {
        if (isBlank(className)) {
            return false;
        }
        String packageName = resolvePackageName(className);
        boolean accepted = packageName.equals(this.packageName);
        if (!accepted && includedSubPackages) {
            accepted = packageName.startsWith(subPackageNamePrefix);
        }
        return accepted;
    }
}
