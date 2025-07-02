
package com.aeotrade.chain.contract.message;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RSAKeyValueType complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="RSAKeyValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Modulus" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;element name="Exponent" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RSAKeyValueType", propOrder = {
    "modulus",
    "exponent"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class RSAKeyValueType {

    @XmlElement(name = "Modulus", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] modulus;
    @XmlElement(name = "Exponent", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] exponent;

    /**
     * 获取modulus属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getModulus() {
        return modulus;
    }

    /**
     * 设置modulus属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setModulus(byte[] value) {
        this.modulus = value;
    }

    /**
     * 获取exponent属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getExponent() {
        return exponent;
    }

    /**
     * 设置exponent属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setExponent(byte[] value) {
        this.exponent = value;
    }

}
