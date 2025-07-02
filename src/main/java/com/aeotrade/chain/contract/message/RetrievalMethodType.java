
package com.aeotrade.chain.contract.message;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RetrievalMethodType complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="RetrievalMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transforms" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrievalMethodType", propOrder = {
    "transforms"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class RetrievalMethodType {

    @XmlElement(name = "Transforms")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected TransformsType transforms;
    @XmlAttribute(name = "URI")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String uri;
    @XmlAttribute(name = "Type")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String type;

    /**
     * 获取transforms属性的值。
     * 
     * @return
     *     possible object is
     *     {@link TransformsType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public TransformsType getTransforms() {
        return transforms;
    }

    /**
     * 设置transforms属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link TransformsType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setTransforms(TransformsType value) {
        this.transforms = value;
    }

    /**
     * 获取uri属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getURI() {
        return uri;
    }

    /**
     * 设置uri属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setURI(String value) {
        this.uri = value;
    }

    /**
     * 获取type属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getType() {
        return type;
    }

    /**
     * 设置type属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2025-06-17T03:59:30+08:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setType(String value) {
        this.type = value;
    }

}
