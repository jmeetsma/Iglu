package org.ijsberg.iglu.configuration.sample.shop;

/**
 */
public interface Shop {

	Product findProductById(long id);

	String getName();

	void collectPhotos(String orderCode);
}
