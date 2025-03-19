/**
 *
 */
package io.microsphere.filter;

import static io.microsphere.util.ClassUtils.resolvePackageName;
import static io.microsphere.util.StringUtils.isBlank;

/**
 * {@link PackageNameClassNameFilter}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
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
        this.subPackageNamePrefix = includedSubPackages ? packageName + "." : null;
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
