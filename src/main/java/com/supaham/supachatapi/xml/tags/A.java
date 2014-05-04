package com.supaham.supachatapi.xml.tags;

import com.supaham.supachatapi.fanciful.FancyMessage;
import com.supaham.supachatapi.xml.Element;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class A extends Element {
    @XmlAttribute(required = true)
    private String href;

    @Override
    protected void modifyStyle(FancyMessage fancyMessage, Object... params) {
        super.modifyStyle(fancyMessage, params);
        fancyMessage.link(href);
    }
}
