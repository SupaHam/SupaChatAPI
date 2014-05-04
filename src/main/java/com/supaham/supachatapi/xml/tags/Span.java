package com.supaham.supachatapi.xml.tags;

import com.supaham.supachatapi.fanciful.FancyMessage;
import com.supaham.supachatapi.xml.Element;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Span extends Element {
    @Override
    protected void modifyStyle(FancyMessage fancyMessage, Object... params) {
        super.modifyStyle(fancyMessage, params);
    }
}
