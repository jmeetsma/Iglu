/*
 * Copyright 2011 Jeroen Meetsma
 *
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.sample.configuration;

import java.io.Serializable;

public class Apple implements AppleInterface {

	private String message;
	private BananaInterface banana;
	private int someInt;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		//invoked by setProperties
		this.message = message;
	}

	public Object returnInput(Object input) {
		return input;
	}


	public void setBanana(BananaInterface banana) {
		this.banana = banana;
	}

	public void setBanana(Serializable banana) {
	}

	public int getIntFromBanana() {
		return banana.returnAnInt();
	}

	public void setSomeInt(int someInt) {
		this.someInt = someInt;
	}

	public int getSomeInt() {
		return someInt;
	}

	protected void touchCore() {
	}

}
