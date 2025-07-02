
package com.aeotrade.chain.contract.message;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Data complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="Data">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>base64Binary">
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Data", namespace = "http://www.chinaport.gov.cn/dxp", propOrder = {
    "value"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class Data {

    @XmlValue
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] value;

    /**
     * 获取value属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getValue() {
        return value;
    }

    /**
     * 设置value属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setValue(byte[] value) {
        this.value = value;
    }

}
