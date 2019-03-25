package cn.fxlcy.simpleskin;

import dalvik.system.DexFile;

final class FixClassLoader extends ClassLoader {
    private DexFile mDexFile;

    FixClassLoader(ClassLoader parent, DexFile dexFile) {
        super(parent);
        this.mDexFile = dexFile;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return mDexFile.loadClass(name, this);
    }
}
