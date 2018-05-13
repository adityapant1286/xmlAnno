package com.xmlanno;

import static com.xmlanno.utils.XmlAnnoUtil.hasText;

public final class XmlAnnoFactory {

    private XmlAnnoFactory() { }

    private static final class XmlAnnoFactoryHolder {
        private static final XmlAnnoFactory INSTANCE = new XmlAnnoFactory();
    }

    public void build(String scanPackage) {

        if (!hasText(scanPackage))
            throw new IllegalArgumentException("Input parameter is null");


    }
}
