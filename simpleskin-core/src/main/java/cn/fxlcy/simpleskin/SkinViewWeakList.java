package cn.fxlcy.simpleskin;

import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

final class SkinViewWeakList implements Iterable<SkinView> {
    private final List<SkinView> mSkinViews = new LinkedList<>();


    public void add(View view, List<SkinViewAttr> attrs) {
        mSkinViews.add(new SkinView(view, attrs));
    }

    private void expungeStaleEntries() {
        final Iterator<SkinView> each = mSkinViews.iterator();
        while (each.hasNext()) {
            SkinView v = each.next();
            if (!v.valid()) {
                each.remove();
            }
        }
    }


    @NotNull
    @Override
    public Iterator<SkinView> iterator() {
        expungeStaleEntries();
        return new SkinViewIterator();
    }


    private class SkinViewIterator implements Iterator<SkinView> {
        private Iterator<SkinView> base = mSkinViews.iterator();

        @Override
        public boolean hasNext() {
            return base.hasNext();
        }

        @Override
        public SkinView next() {
            return base.next();
        }
    }
}
