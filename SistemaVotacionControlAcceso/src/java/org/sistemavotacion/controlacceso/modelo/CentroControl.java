package org.sistemavotacion.controlacceso.modelo;

import java.io.Serializable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
/**
* @author jgzornoza
* Licencia: http://bit.ly/j9jZQH
*/
@Entity
@Table(name="CentroControl")
@DiscriminatorValue("CentroControl")
public class CentroControl extends ActorConIP implements Serializable {

    public static final long serialVersionUID = 1L;


}