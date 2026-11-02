# The library resolves generic type arguments through genericSuperclass at runtime.
# R8 full mode only retains generic signatures for explicitly kept classes.
-keepattributes Signature

# Prevent class merging from changing the generic inheritance chain inspected by
# the ViewBinding base classes. This replaces the historical global class-merging
# exclusion and constructor rules with targeted class rules. Unused classes may
# still be removed and names may still be obfuscated.
-keep,allowshrinking,allowobfuscation class com.bonepeople.android.base.viewbinding.ViewBinding**
-keep,allowshrinking,allowobfuscation class * extends com.bonepeople.android.base.viewbinding.ViewBinding**

# ViewBinding implementations are generic signature endpoints. Their inflate
# methods are looked up by name and invoked through reflection.
-keep,allowshrinking,allowobfuscation class * implements androidx.viewbinding.ViewBinding
-keepclassmembers,allowoptimization class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(android.view.LayoutInflater);
    public static *** inflate(android.view.LayoutInflater,android.view.ViewGroup,boolean);
}

# BaseUserManager resolves its data type from the concrete subclass signature.
-keep,allowshrinking,allowobfuscation class com.bonepeople.android.base.manager.BaseUserManager
-keep,allowshrinking,allowobfuscation class * extends com.bonepeople.android.base.manager.BaseUserManager