/*
 * This file is part of the Translation Tools, modified on 25.08.17 19:57.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

/*
 * This file is part of the Translation Tools, modified on 25.08.17 19:57.
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package status_image;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

/**
 * This class is an BufferedImageImage which width is extended by a given value.
 */
class ExtendedBufferedImage extends BufferedImage {

    /**
     * Constructs a <code>BufferedImage</code> of one of the predefined image types.  The <code>ColorSpace</code> for
     * the image is the default sRGB space.
     *
     * @param oldImage      the old image that is to be extended
     * @param widthAddition the width in pixels that is added to the width of the given old image
     * @see ColorSpace
     * @see #TYPE_INT_RGB
     * @see #TYPE_INT_ARGB
     * @see #TYPE_INT_ARGB_PRE
     * @see #TYPE_INT_BGR
     * @see #TYPE_3BYTE_BGR
     * @see #TYPE_4BYTE_ABGR
     * @see #TYPE_4BYTE_ABGR_PRE
     * @see #TYPE_BYTE_GRAY
     * @see #TYPE_USHORT_GRAY
     * @see #TYPE_BYTE_BINARY
     * @see #TYPE_BYTE_INDEXED
     * @see #TYPE_USHORT_565_RGB
     * @see #TYPE_USHORT_555_RGB
     */
    ExtendedBufferedImage(BufferedImage oldImage, int widthAddition) {
        super(oldImage.getWidth() + widthAddition, oldImage.getHeight(), oldImage.getType());

        // Copy all pixels from the old image into the new one
        for (int i = 0; i < oldImage.getWidth(); i++) {
            for (int j = 0; j < oldImage.getHeight(); j++) {
                setRGB(i, j, oldImage.getRGB(i, j));
            }
        }

        // Create a column separator on the right size of the old pixels
        for (int i = 0; i < getHeight(); i++) {
            setRGB(oldImage.getWidth(), i, Color.BLACK.getRGB());
        }
    }
}
