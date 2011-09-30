package net.link.util.data;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;


public class Data implements Serializable {

    @XmlAttribute
    public String location;

    public byte[] data;
}
