
package com.aeotrade.chain.contract.message;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>DSAKeyValueType complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="DSAKeyValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="P" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *           &lt;element name="Q" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;/sequence>
 *         &lt;element name="G" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary" minOccurs="0"/>
 *         &lt;element name="Y" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;element name="J" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="Seed" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *           &lt;element name="PgenCounter" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DSAKeyValueType", propOrder = {
    "p",
    "q",
    "g",
    "y",
    "j",
    "seed",
    "pgenCounter"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class DSAKeyValueType {

    @XmlElement(name = "P")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] p;
    @XmlElement(name = "Q")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] q;
    @XmlElement(name = "G")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] g;
    @XmlElement(name = "Y", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] y;
    @XmlElement(name = "J")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] j;
    @XmlElement(name = "Seed")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] seed;
    @XmlElement(name = "PgenCounter")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected byte[] pgenCounter;

    /**
     * 获取p属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getP() {
        return p;
    }

    /**
     * 设置p属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setP(byte[] value) {
        this.p = value;
    }

    /**
     * 获取q属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getQ() {
        return q;
    }

    /**
     * 设置q属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setQ(byte[] value) {
        this.q = value;
    }

    /**
     * 获取g属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getG() {
        return g;
    }

    /**
     * 设置g属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setG(byte[] value) {
        this.g = value;
    }

    /**
     * 获取y属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getY() {
        return y;
    }

    /**
     * 设置y属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setY(byte[] value) {
        this.y = value;
    }

    /**
     * 获取j属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getJ() {
        return j;
    }

    /**
     * 设置j属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setJ(byte[] value) {
        this.j = value;
    }

    /**
     * 获取seed属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getSeed() {
        return seed;
    }

    /**
     * 设置seed属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setSeed(byte[] value) {
        this.seed = value;
    }

    /**
     * 获取pgenCounter属性的值。
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public byte[] getPgenCounter() {
        return pgenCounter;
    }

    /**
     * 设置pgenCounter属性的值。
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setPgenCounter(byte[] value) {
        this.pgenCounter = value;
    }

}
