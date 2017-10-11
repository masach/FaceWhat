package com.facewhat.fwarchive.xep0136;

import java.util.Date;

import org.dom4j.Element;
import org.dom4j.QName;

import com.facewhat.fwarchive.util.XmppDateUtil;
import com.facewhat.fwarchive.xep0059.XmppResultSet;

/**
 * A request to retrieve a collection.
 */
public class FWRetrieveRequest
{
	// 添加的 end,keyworkd, 
	// nickname 用于muc  
    private String with;
    private Date start;
    private Date end;
    private String keyWord;
    private String nickname;

    private XmppResultSet resultSet;

    public FWRetrieveRequest(Element listElement)
    {
        this.with = listElement.attributeValue("with");
        this.start = XmppDateUtil.parseDate(listElement.attributeValue("start"));
        this.end = XmppDateUtil.parseDate(listElement.attributeValue("end"));
        this.keyWord = listElement.attributeValue("keyword");
        this.nickname = listElement.attributeValue("nickname");
        

        Element setElement = listElement.element(QName.get("set", XmppResultSet.NAMESPACE));
        if (setElement != null)
        {
            resultSet = new XmppResultSet(setElement);
        }
//        1398250549490
//        1491274490273
    }

    
    public String getNickname() {
		return nickname;
	}


	public Date getEnd() {
		return end;
	}


	public String getKeyWord() {
		return keyWord;
	}


	public String getWith()
    {
        return with;
    }

    public Date getStart()
    {
        return start;
    }

    public XmppResultSet getResultSet()
    {
        return resultSet;
    }
}
