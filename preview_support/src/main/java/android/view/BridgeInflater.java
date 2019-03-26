//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.ResolvingAttributeSet;
import android.view.View.OnAttachStateChangeListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.ide.common.rendering.api.LayoutlibCallback;
import com.android.ide.common.rendering.api.MergeCookie;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.MockView;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.android.BridgeXmlBlockParser;
import com.android.layoutlib.bridge.android.support.DrawerLayoutUtil;
import com.android.layoutlib.bridge.android.support.RecyclerViewUtil;
import com.android.layoutlib.bridge.impl.ParserFactory;
import com.android.layoutlib.bridge.util.ReflectionUtils;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class BridgeInflater extends LayoutInflater {
    private static final String INFLATER_CLASS_ATTR_NAME = "viewInflaterClass";
    private static final ResourceReference RES_AUTO_INFLATER_CLASS_ATTR;
    private static final ResourceReference LEGACY_APPCOMPAT_INFLATER_CLASS_ATTR;
    private static final ResourceReference ANDROIDX_APPCOMPAT_INFLATER_CLASS_ATTR;
    private static final String LEGACY_DEFAULT_APPCOMPAT_INFLATER_NAME = "android.support.v7.app.AppCompatViewInflater";
    private static final String ANDROIDX_DEFAULT_APPCOMPAT_INFLATER_NAME = "androidx.appcompat.app.AppCompatViewInflater";
    private final LayoutlibCallback mLayoutlibCallback;
    private boolean mIsInMerge = false;
    private ResourceReference mResourceReference;
    private Map<View, String> mOpenDrawerLayouts;
    private static final int[] ATTRS_THEME;
    private static final String[] sClassPrefixList;
    private BiFunction<String, AttributeSet, View> mCustomInflater;

    public static String[] getClassPrefixList() {
        return sClassPrefixList;
    }

    private BridgeInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
        newContext = BridgeContext.getBaseContext(newContext);
        this.mLayoutlibCallback = newContext instanceof BridgeContext ? ((BridgeContext) newContext).getLayoutlibCallback() : null;

        SimpleLayoutAccess.init(mLayoutlibCallback);
    }

    public BridgeInflater(BridgeContext context, LayoutlibCallback layoutlibCallback) {
        super(context);
        this.mLayoutlibCallback = layoutlibCallback;
        this.mConstructorArgs[0] = context;

        SimpleLayoutAccess.init(layoutlibCallback);
    }

    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        View view = this.createViewFromCustomInflater(name, attrs);
        if (view == null) {
            try {
                String[] var4 = sClassPrefixList;
                int var5 = var4.length;

                for (int var6 = 0; var6 < var5; ++var6) {
                    String prefix = var4[var6];

                    try {
                        view = this.createView(name, prefix, attrs);
                        if (view != null) {
                            break;
                        }
                    } catch (ClassNotFoundException var10) {
                        ;
                    }
                }

                try {
                    if (view == null) {
                        view = super.onCreateView(name, attrs);
                    }
                } catch (ClassNotFoundException var9) {
                    ;
                }

                if (view == null) {
                    view = this.loadCustomView(name, attrs);
                }
            } catch (InflateException var11) {
                throw var11;
            } catch (Exception var12) {
                throw new ClassNotFoundException("onCreateView", var12);
            }
        }

        this.setupViewInContext(view, attrs);


        if (view != null) {
            Drawable drawable = SimpleLayoutAccess.commonDrawableInflater(view, getContext(), attrs);
            if (drawable != null) {
                view.setBackground(drawable);
            }

            if (view instanceof TextView) {
                ColorStateList colorStateList = SimpleLayoutAccess.commonTextColorInflater(getContext(), attrs);
                if (colorStateList != null) {
                    ((TextView) view).setTextColor(colorStateList);
                }
            }
        }

        return view;
    }

    private static Method getCreateViewMethod(Class<?> customInflaterClass) throws NoSuchMethodException {
        Class current = customInflaterClass;

        while (true) {
            try {
                Method method = current.getDeclaredMethod("createView", View.class, String.class, Context.class, AttributeSet.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException var3) {
                current = current.getSuperclass();
                if (current == null || current == Object.class) {
                    throw new NoSuchMethodException();
                }
            }
        }
    }

    private static Class<?> findCustomInflater(BridgeContext bc, LayoutlibCallback layoutlibCallback) {
        ResourceReference attrRef;
        if (layoutlibCallback.isResourceNamespacingRequired()) {
            if (layoutlibCallback.hasLegacyAppCompat()) {
                attrRef = LEGACY_APPCOMPAT_INFLATER_CLASS_ATTR;
            } else {
                if (!layoutlibCallback.hasAndroidXAppCompat()) {
                    return null;
                }

                attrRef = ANDROIDX_APPCOMPAT_INFLATER_CLASS_ATTR;
            }
        } else {
            attrRef = RES_AUTO_INFLATER_CLASS_ATTR;
        }

        ResourceValue value = bc.getRenderResources().findItemInTheme(attrRef);
        String inflaterName = value != null ? value.getValue() : null;
        if (inflaterName != null) {
            try {
                return layoutlibCallback.findClass(inflaterName);
            } catch (ClassNotFoundException var7) {
                ;
            }
        } else if (bc.isAppCompatTheme()) {
            try {
                if (layoutlibCallback.hasLegacyAppCompat()) {
                    return layoutlibCallback.findClass("android.support.v7.app.AppCompatViewInflater");
                }

                if (layoutlibCallback.hasAndroidXAppCompat()) {
                    return layoutlibCallback.findClass("androidx.appcompat.app.AppCompatViewInflater");
                }
            } catch (ClassNotFoundException var6) {
                ;
            }
        }

        return null;
    }

    private View createViewFromCustomInflater(String name, AttributeSet attrs) {
        if (this.mCustomInflater == null) {
            Context context = this.getContext();
            context = BridgeContext.getBaseContext(context);
            if (context instanceof BridgeContext) {
                BridgeContext bc = (BridgeContext) context;
                Class<?> inflaterClass = findCustomInflater(bc, this.mLayoutlibCallback);
                if (inflaterClass != null) {
                    try {
                        Constructor<?> constructor = inflaterClass.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        Object inflater = constructor.newInstance();
                        Method method = getCreateViewMethod(inflaterClass);
                        this.mCustomInflater = (viewName, attributeSet) -> {
                            try {
                                return (View) method.invoke(inflater, null, viewName, bc, attributeSet, false, false, true, true);
                            } catch (InvocationTargetException | IllegalAccessException var6) {
                                assert false : "Call to createView failed";

                                return null;
                            }
                        };
                    } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException var10) {
                        ;
                    }
                }
            }

            if (this.mCustomInflater == null) {
                this.mCustomInflater = (s, attributeSet) -> {
                    return null;
                };
            }
        }

        return (View) this.mCustomInflater.apply(name, attrs);
    }

    public View createViewFromTag(View parent, String name, Context context, AttributeSet attrs, boolean ignoreThemeAttr) {
        View view = null;
        if (name.equals("view")) {
            name = attrs.getAttributeValue((String) null, "class");
            if (name == null) {
                Bridge.getLog().error("broken", "Unable to inflate view tag without class attribute", (Object) null);
                view = new MockView((Context) context, attrs);
                ((MockView) view).setText("view");
            }
        }

        try {
            if (view == null) {
                view = super.createViewFromTag(parent, name, (Context) context, attrs, ignoreThemeAttr);
            }
        } catch (InflateException var17) {
            InflateException e = var17;
            if (!ignoreThemeAttr) {
                TypedArray ta = ((Context) context).obtainStyledAttributes(attrs, ATTRS_THEME);
                int themeResId = ta.getResourceId(0, 0);
                if (themeResId != 0) {
                    context = new ContextThemeWrapper((Context) context, themeResId);
                }

                ta.recycle();
            }

            if (!(var17.getCause() instanceof ClassNotFoundException)) {
                view = new MockView((Context) context, attrs);
                ((MockView) view).setText(name);
                Bridge.getLog().error("broken", var17.getMessage(), var17, (Object) null);
            } else {
                Object lastContext = this.mConstructorArgs[0];
                this.mConstructorArgs[0] = context;

                try {
                    view = this.loadCustomView(name, attrs);
                } catch (Exception var15) {
                    InflateException exception = new InflateException();
                    if (!var15.getClass().equals(ClassNotFoundException.class)) {
                        exception.initCause(var15);
                    } else {
                        exception.initCause(e);
                    }

                    throw exception;
                } finally {
                    this.mConstructorArgs[0] = lastContext;
                }
            }
        }

        this.setupViewInContext((View) view, attrs);
        return (View) view;
    }

    public View inflate(int resource, ViewGroup root) {
        Context context = this.getContext();
        context = BridgeContext.getBaseContext(context);
        if (context instanceof BridgeContext) {
            BridgeContext bridgeContext = (BridgeContext) context;
            ResourceValue value = null;
            ResourceReference layoutInfo = Bridge.resolveResourceId(resource);
            if (layoutInfo == null) {
                layoutInfo = this.mLayoutlibCallback.resolveResourceId(resource);
            }

            if (layoutInfo != null) {
                value = bridgeContext.getRenderResources().getResolvedResource(layoutInfo);
            }

            if (value != null) {
                String path = value.getValue();

                try {
                    XmlPullParser parser = ParserFactory.create(path, true);
                    if (parser == null) {
                        return null;
                    }

                    BridgeXmlBlockParser bridgeParser = new BridgeXmlBlockParser(parser, bridgeContext, value.getNamespace());
                    return this.inflate(bridgeParser, root);
                } catch (Exception var10) {
                    Bridge.getLog().error("resources.read", "Failed to parse file " + path, var10, (Object) null);
                    return null;
                }
            }
        }

        return null;
    }

    private View loadCustomView(String name, AttributeSet attrs, boolean silent) throws Exception {
        if (this.mLayoutlibCallback != null) {
            if (name.equals("view")) {
                name = attrs.getAttributeValue((String) null, "class");
                if (name == null) {
                    return null;
                }
            }

            this.mConstructorArgs[1] = attrs;
            Object customView = silent ? this.mLayoutlibCallback.loadClass(name, mConstructorSignature, this.mConstructorArgs) : this.mLayoutlibCallback.loadView(name, mConstructorSignature, this.mConstructorArgs);
            if (customView instanceof View) {
                return (View) customView;
            }
        }

        return null;
    }

    private View loadCustomView(String name, AttributeSet attrs) throws Exception {
        return this.loadCustomView(name, attrs, false);
    }

    private void setupViewInContext(final View view, AttributeSet attrs) {
        Context context = this.getContext();
        context = BridgeContext.getBaseContext(context);
        if (context instanceof BridgeContext) {
            BridgeContext bc = (BridgeContext) context;
            Object viewKey = getViewKeyFromParser(attrs, bc, this.mResourceReference, this.mIsInMerge);
            if (viewKey != null) {
                bc.addViewKey(view, viewKey);
            }

            String scrollPosX = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "scrollX");
            if (scrollPosX != null && scrollPosX.endsWith("px")) {
                int value = Integer.parseInt(scrollPosX.substring(0, scrollPosX.length() - 2));
                bc.setScrollXPos(view, value);
            }

            String scrollPosY = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "scrollY");
            int resourceId;
            if (scrollPosY != null && scrollPosY.endsWith("px")) {
                resourceId = Integer.parseInt(scrollPosY.substring(0, scrollPosY.length() - 2));
                bc.setScrollYPos(view, resourceId);
            }

            if (ReflectionUtils.isInstanceOf(view, RecyclerViewUtil.CN_RECYCLER_VIEW)) {
                resourceId = 0;
                int attrItemCountValue = attrs.getAttributeIntValue("http://schemas.android.com/tools", "itemCount", -1);
                if (attrs instanceof ResolvingAttributeSet) {
                    ResourceValue attrListItemValue = ((ResolvingAttributeSet) attrs).getResolvedAttributeValue("http://schemas.android.com/tools", "listitem");
                    if (attrListItemValue != null) {
                        resourceId = bc.getResourceId(attrListItemValue.asReference(), 0);
                    }
                }

                RecyclerViewUtil.setAdapter(view, bc, this.mLayoutlibCallback, resourceId, attrItemCountValue);
            } else {
                String visibility;
                if (ReflectionUtils.isInstanceOf(view, DrawerLayoutUtil.CN_DRAWER_LAYOUT)) {
                    visibility = attrs.getAttributeValue("http://schemas.android.com/tools", "openDrawer");
                    if (visibility != null) {
                        this.getDrawerLayoutMap().put(view, visibility);
                    }
                } else if (view instanceof NumberPicker) {
                    NumberPicker numberPicker = (NumberPicker) view;
                    String minValue = attrs.getAttributeValue("http://schemas.android.com/tools", "minValue");
                    if (minValue != null) {
                        numberPicker.setMinValue(Integer.parseInt(minValue));
                    }

                    String maxValue = attrs.getAttributeValue("http://schemas.android.com/tools", "maxValue");
                    if (maxValue != null) {
                        numberPicker.setMaxValue(Integer.parseInt(maxValue));
                    }
                } else if (view instanceof ImageView) {
                    ImageView img = (ImageView) view;
                    Drawable drawable = img.getDrawable();
                    if (drawable instanceof Animatable && !((Animatable) drawable).isRunning()) {
                        ((Animatable) drawable).start();
                    }
                } else if (view instanceof ViewStub) {
                    visibility = attrs.getAttributeValue("http://schemas.android.com/tools", "visibility");
                    boolean isVisible = "visible".equals(visibility);
                    if (isVisible || "invisible".equals(visibility)) {
                        final int visibilityValue = isVisible ? 0 : 4;
                        view.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                            public void onViewAttachedToWindow(View v) {
                                v.removeOnAttachStateChangeListener(this);
                                view.setVisibility(visibilityValue);
                            }

                            public void onViewDetachedFromWindow(View v) {
                            }
                        });
                    }
                }
            }

        }
    }

    public void setIsInMerge(boolean isInMerge) {
        this.mIsInMerge = isInMerge;
    }

    public void setResourceReference(ResourceReference reference) {
        this.mResourceReference = reference;
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new BridgeInflater(this, newContext);
    }

    static Object getViewKeyFromParser(AttributeSet attrs, BridgeContext bc, ResourceReference resourceReference, boolean isInMerge) {
        if (!(attrs instanceof BridgeXmlBlockParser)) {
            return null;
        } else {
            BridgeXmlBlockParser parser = (BridgeXmlBlockParser) attrs;
            Object viewKey = parser.getViewCookie();
            if (viewKey == null) {
                int currentDepth = parser.getDepth();
                BridgeXmlBlockParser previousParser = bc.getPreviousParser();
                if (previousParser != null) {
                    int testDepth = isInMerge ? 2 : 1;
                    if (currentDepth == testDepth) {
                        viewKey = previousParser.getViewCookie();
                        if (viewKey != null && isInMerge) {
                            viewKey = new MergeCookie(viewKey);
                        }
                    }
                } else if (resourceReference != null && currentDepth == 1) {
                    viewKey = resourceReference;
                }
            }

            return viewKey;
        }
    }

    public void postInflateProcess(View view) {
        if (this.mOpenDrawerLayouts != null) {
            String gravity = (String) this.mOpenDrawerLayouts.get(view);
            if (gravity != null) {
                DrawerLayoutUtil.openDrawer(view, gravity);
            }

            this.mOpenDrawerLayouts.remove(view);
        }

    }

    private Map<View, String> getDrawerLayoutMap() {
        if (this.mOpenDrawerLayouts == null) {
            this.mOpenDrawerLayouts = new HashMap(4);
        }

        return this.mOpenDrawerLayouts;
    }

    public void onDoneInflation() {
        if (this.mOpenDrawerLayouts != null) {
            this.mOpenDrawerLayouts.clear();
        }

    }

    static {
        RES_AUTO_INFLATER_CLASS_ATTR = ResourceReference.attr(ResourceNamespace.RES_AUTO, "viewInflaterClass");
        LEGACY_APPCOMPAT_INFLATER_CLASS_ATTR = ResourceReference.attr(ResourceNamespace.APPCOMPAT_LEGACY, "viewInflaterClass");
        ANDROIDX_APPCOMPAT_INFLATER_CLASS_ATTR = ResourceReference.attr(ResourceNamespace.APPCOMPAT, "viewInflaterClass");
        ATTRS_THEME = new int[]{16842752};
        sClassPrefixList = new String[]{"android.widget.", "android.webkit.", "android.app."};
    }
}
