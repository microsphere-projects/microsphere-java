/**
 *
 */
package io.microsphere.filter;

import static io.microsphere.constants.SymbolConstants.DOT;

/**
 * {@link ClassFilter} implementation that filters classes based on their package name.
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Create a filter for classes in the "io.microsphere" package only
 * PackageNameClassFilter filter = new PackageNameClassFilter("io.microsphere", false);
 *
 * // Create a filter for classes in the "io.microsphere" package and its sub-packages
 * PackageNameClassFilter filter = new PackageNameClassFilter("io.microsphere", true);
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassFilter
 * @since 1.0.0
 */
public class PackageNameClassFilter implements ClassFilter {

    private final String packageName;
    private final boolean includedSubPackages;
    private final String subPackageNamePrefix;

    /**
     * Constructor
     *
     * @param packageName         the name of package
     * @param includedSubPackages included sub-packages
     */
    public PackageNameClassFilter(String packageName, boolean includedSubPackages) {
        this.packageName = packageName;
        this.includedSubPackages = includedSubPackages;
        this.subPackageNamePrefix = includedSubPackages ? packageName + DOT : null;
    }

    @Override
    public boolean accept(Class<?> filteredObject) {
        if (filteredObject == null) {
            return false;
        }
        Package package_ = filteredObject.getPackage();
        String packageName = package_.getName();
        boolean accepted = packageName.equals(this.packageName);
        if (!accepted && includedSubPackages) {
            accepted = packageName.startsWith(subPackageNamePrefix);
        }
        return accepted;
    }
}
