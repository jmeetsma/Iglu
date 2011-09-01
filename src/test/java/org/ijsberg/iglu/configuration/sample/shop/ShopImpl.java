package org.ijsberg.iglu.configuration.sample.shop;

/**
 */
public class ShopImpl implements Shop
{
	private String name;
	private PhotoPrintService photoPrintService;

	public ShopImpl(String name) {
		this.name = name;
	}

	public Product findProductById(long id) {
		return null;
	}

	public String getName() {
		return name;
	}

	public void collectPhotos(String orderCode) {
	}

	public void setPhotoPrintService(PhotoPrintService photoPrintService) {
		this.photoPrintService = photoPrintService;
	}

	public boolean hasPhotoPrintService() {
		return photoPrintService != null;
	}
}
