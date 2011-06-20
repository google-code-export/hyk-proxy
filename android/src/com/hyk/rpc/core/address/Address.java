/**
 * 
 */
package com.hyk.rpc.core.address;

import java.io.Serializable;

/**
 * @author Administrator
 *
 */
public interface Address extends Serializable {
	 public boolean equals(Object obj);
	 public int hashCode();
	 public String toPrintableString();
}
