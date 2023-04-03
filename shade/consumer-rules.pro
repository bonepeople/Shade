#ViewBinding
-keepclassmembers class * implements androidx.viewbinding.ViewBinding{
  inflate(android.view.LayoutInflater);
  inflate(android.view.LayoutInflater,android.view.ViewGroup,boolean);
}

#BaseUserManager
-keepclassmembers class * extends com.bonepeople.android.base.manager.BaseUserManager{
 <init>(...);
}

#ViewBindingRecyclerAdapter
-keepclassmembers class * extends com.bonepeople.android.base.viewbinding.ViewBindingRecyclerAdapter{
 <init>(...);
}

#ViewBindingRefreshAdapter
-keepclassmembers class * extends com.bonepeople.android.base.viewbinding.ViewBindingRefreshAdapter{
 <init>(...);
}