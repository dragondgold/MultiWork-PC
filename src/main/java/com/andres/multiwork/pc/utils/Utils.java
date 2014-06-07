package com.andres.multiwork.pc.utils;

/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import static javafx.scene.control.OverrunStyle.*;
import javafx.geometry.Point2D;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Utils {

    static Text helper = new Text();

    public static double computeTextWidth(Font font, String text, double wrappingWidth) {
        helper.setText(text);
        helper.setFont(font);
        // Note that the wrapping width needs to be set to zero before
        // getting the text's real preferred width.
        helper.setWrappingWidth(0);
        double w = Math.min(helper.prefWidth(-1), wrappingWidth);
        helper.setWrappingWidth((int)Math.ceil(w));
        return Math.ceil(helper.getLayoutBounds().getWidth());
    }

    public static double computeTextHeight(Font font, String text, double wrappingWidth) {
        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth((int)wrappingWidth);
        return helper.getLayoutBounds().getHeight();
    }

    public static String computeClippedText(Font font, String text, double width, OverrunStyle type, String ellipsisString) {
        if (font == null) {
            throw new IllegalArgumentException("Must specify a font");
        }
        OverrunStyle style = (type == null || OverrunStyle.CLIP.equals(type)) ? (OverrunStyle.ELLIPSIS) : (type);
        String ellipsis = (style == CLIP) ? "" : ellipsisString;
        // if the text is empty or null or no ellipsis, then it always fits
        if (text == null || "".equals(text)) {
            return text;
        }
        // if the string width is < the available width, then it fits and
        // doesn't need to be clipped.  We use a double point comparison
        // of 0.001 (1/1000th of a pixel) to account for any numerical
        // discrepancies introduced when the available width was calculated.
        // MenuItemSkinBase.doLayout, for example, does a number of double
        // point operations when computing the available width.
        final double stringWidth = computeTextWidth(font, text, 0);
        if (stringWidth - width < 0.0010F) {
            return text;
        }
        // the width used by the ellipsis string
        final double ellipsisWidth = computeTextWidth(font, ellipsis, 0);
        // the available maximum width to fit chars into. This is essentially
        // the width minus the space required for the E ellipsis string
        final double availableWidth = width - ellipsisWidth;

        if (width < ellipsisWidth) {
            // The ellipsis doesn't fit.
            return "";
        }

        // if we got here, then we must clip the text with an ellipsis.
        // this can be pretty expensive depending on whether "complex" text
        // layout needs to be taken into account. So each ellipsis option has
        // to take into account two code paths: the easy way and the correct
        // way. This is flagged by the "complexLayout" boolean
        // TODO make sure this function call takes into account ligatures, kerning,
        // and such as that will change the layout characteristics of the text
        // and will require a full complex layout
        // TODO since we don't have all the stuff available in FX to determine
        // complex text, I'm going to for now assume complex text is always false.
        final boolean complexLayout = false;
        //requiresComplexLayout(font, text);

        // generally all we want to do is count characters and add their widths.
        // For ellipses which break on words, we do NOT want to include any
        // hanging whitespace.
        if (style.equals(OverrunStyle.ELLIPSIS) ||
                style.equals(OverrunStyle.WORD_ELLIPSIS) ||
                style.equals(OverrunStyle.LEADING_ELLIPSIS) ||
                style.equals(OverrunStyle.LEADING_WORD_ELLIPSIS)) {

            final boolean wordTrim = OverrunStyle.WORD_ELLIPSIS.equals(style) || OverrunStyle.LEADING_WORD_ELLIPSIS.equals(style);
            String substring;
            if (complexLayout) {
            }  else //            AttributedString a = new AttributedString(text);
            //            LineBreakMeasurer m = new LineBreakMeasurer(a.getIterator(), frc);
            //            substring = text.substring(0, m.nextOffset((double)availableWidth));
            {
                // simply total up the widths of all chars to determine how many
                // will fit in the available space. Remember the last whitespace
                // encountered so that if we're breaking on words we can trim
                // and omit it.
                double total = 0.0F;
                int whitespaceIndex = -1;
                // at the termination of the loop, index will be one past the
                // end of the substring
                int index = 0;
                int start = (style.equals(OverrunStyle.LEADING_ELLIPSIS) || style.equals(OverrunStyle.LEADING_WORD_ELLIPSIS)) ? (text.length() - 1) : (0);
                int end = (start == 0) ? (text.length() - 1) : (0);
                int stepValue = (start == 0) ? (1) : (-1);
                boolean done = start == 0? start > end : start < end;
                for (int i = start; !done ; i += stepValue) {
                    index = i;
                    char c = text.charAt(index);
                    total = computeTextWidth(font,
                            (start == 0) ? text.substring(0, i + 1)
                                    : text.substring(i, start + 1),
                            0);
                    if (Character.isWhitespace(c)) {
                        whitespaceIndex = index;
                    }
                    if (total > availableWidth) {
                        break;
                    }
                    done = start == 0? i >= end : i <= end;
                }
                final boolean fullTrim = !wordTrim || whitespaceIndex == -1;
                substring = (start == 0) ?
                        (text.substring(0, (fullTrim) ? (index) : (whitespaceIndex))) :
                        (text.substring(((fullTrim) ? (index) : (whitespaceIndex)) + 1));

            }
            if (OverrunStyle.ELLIPSIS.equals(style) || OverrunStyle.WORD_ELLIPSIS.equals(style)) {
                return substring + ellipsis;
            } else {
                //style == LEADING_ELLIPSIS or style == LEADING_WORD_ELLIPSIS
                return ellipsis + substring;
            }
        } else {
            // these two indexes are INCLUSIVE not exclusive
            int leadingIndex = 0;
            int trailingIndex = 0;
            int leadingWhitespace = -1;
            int trailingWhitespace = -1;
            // The complex case is going to be killer. What I have to do is
            // read all the chars from the left up to the leadingIndex,
            // and all the chars from the right up to the trailingIndex,
            // and sum those together to get my total. That is, I cannot have
            // a running total but must retotal the cummulative chars each time
            if (complexLayout) {
            } else /*            double leadingTotal = 0;
               double trailingTotal = 0;
               for (int i=0; i<text.length(); i++) {
               double total = computeStringWidth(metrics, text.substring(0, i));
               if (total + trailingTotal > availableWidth) break;
               leadingIndex = i;
               leadingTotal = total;
               if (Character.isWhitespace(text.charAt(i))) leadingWhitespace = leadingIndex;

               int index = text.length() - (i + 1);
               total = computeStringWidth(metrics, text.substring(index - 1));
               if (total + leadingTotal > availableWidth) break;
               trailingIndex = index;
               trailingTotal = total;
               if (Character.isWhitespace(text.charAt(index))) trailingWhitespace = trailingIndex;
               }*/
            {
                // either CENTER_ELLIPSIS or CENTER_WORD_ELLIPSIS
                // for this case I read one char on the left, then one on the end
                // then second on the left, then second from the end, etc until
                // I have used up all the availableWidth. At that point, I trim
                // the string twice: once from the start to firstIndex, and
                // once from secondIndex to the end. I then insert the ellipsis
                // between the two.
                leadingIndex = -1;
                trailingIndex = -1;
                double total = 0.0F;
                for (int i = 0; i <= text.length() - 1; i++) {
                    char c = text.charAt(i);
                    //total += metrics.charWidth(c);
                    total += computeTextWidth(font, "" + c, 0);
                    if (total > availableWidth) {
                        break;
                    }
                    leadingIndex = i;
                    if (Character.isWhitespace(c)) {
                        leadingWhitespace = leadingIndex;
                    }
                    int index = text.length() - 1 - i;
                    c = text.charAt(index);
                    //total += metrics.charWidth(c);
                    total += computeTextWidth(font, "" + c, 0);
                    if (total > availableWidth) {
                        break;
                    }
                    trailingIndex = index;
                    if (Character.isWhitespace(c)) {
                        trailingWhitespace = trailingIndex;
                    }
                }
            }
            if (leadingIndex < 0) {
                return ellipsis;
            }
            if (OverrunStyle.CENTER_ELLIPSIS.equals(style)) {
                if (trailingIndex < 0) {
                    return text.substring(0, leadingIndex + 1) + ellipsis;
                }
                return text.substring(0, leadingIndex + 1) + ellipsis + text.substring(trailingIndex);
            } else {
                boolean leadingIndexIsLastLetterInWord =
                        Character.isWhitespace(text.charAt(leadingIndex + 1));
                int index = (leadingWhitespace == -1 || leadingIndexIsLastLetterInWord) ? (leadingIndex + 1) : (leadingWhitespace);
                String leading = text.substring(0, index);
                if (trailingIndex < 0) {
                    return leading + ellipsis;
                }
                boolean trailingIndexIsFirstLetterInWord =
                        Character.isWhitespace(text.charAt(trailingIndex - 1));
                index = (trailingWhitespace == -1 || trailingIndexIsFirstLetterInWord) ? (trailingIndex) : (trailingWhitespace + 1);
                String trailing = text.substring(index);
                return leading + ellipsis + trailing;
            }
        }
    }

    public static String computeClippedWrappedText(Font font, String text, double width, double height, OverrunStyle truncationStyle,
                                            String ellipsisString) {
        if (font == null) {
            throw new IllegalArgumentException("Must specify a font");
        }

        String ellipsis = (truncationStyle == CLIP) ? "" : ellipsisString;
        int eLen = ellipsis.length();
        // Do this before using helper, as it's not reentrant.
        double eWidth = computeTextWidth(font, ellipsis, 0);
        double eHeight = computeTextHeight(font, ellipsis, 0);

        if (width < eWidth || height < eHeight) {
            // The ellipsis doesn't fit.
            return "";
        }

        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth((int)Math.ceil(width));

        boolean leading =  (truncationStyle == LEADING_ELLIPSIS ||
                truncationStyle == LEADING_WORD_ELLIPSIS);
        boolean center =   (truncationStyle == CENTER_ELLIPSIS ||
                truncationStyle == CENTER_WORD_ELLIPSIS);
        boolean trailing = !(leading || center);
        boolean wordTrim = (truncationStyle == WORD_ELLIPSIS ||
                truncationStyle == LEADING_WORD_ELLIPSIS ||
                truncationStyle == CENTER_WORD_ELLIPSIS);

        String result = text;
        int len = (result != null) ? result.length() : 0;
        int centerLen = -1;

        Point2D centerPoint = null;
        if (center) {
            // Find index of character in the middle of the visual text area
            centerPoint = new Point2D((width - eWidth) / 2, height / 2 - helper.getBaselineOffset());
        }

        // Find index of character at the bottom left of the text area.
        // This should be the first character of a line that would be clipped.
        Point2D endPoint = new Point2D(0, height - helper.getBaselineOffset());

        int hit = helper.impl_hitTestChar(endPoint).getCharIndex();
        if (hit >= len) {
            return text;
        }
        if (center) {
            hit = helper.impl_hitTestChar(centerPoint).getCharIndex();
        }

        if (hit > 0 && hit < len) {
            // Step one, make a truncation estimate.

            if (center || trailing) {
                int ind = hit;
                if (center) {
                    // This is for the first part, i.e. beginning of text up to ellipsis.
                    if (wordTrim) {
                        int brInd = lastBreakCharIndex(text, ind + 1);
                        if (brInd >= 0) {
                            ind = brInd + 1;
                        } else {
                            brInd = firstBreakCharIndex(text, ind);
                            if (brInd >= 0) {
                                ind = brInd + 1;
                            }
                        }
                    }
                    centerLen = ind + eLen;
                } // else: text node wraps at words, so wordTrim is not needed here.
                result = result.substring(0, ind) + ellipsis;
            }

            if (leading || center) {
                // The hit is an index counted from the beginning, but we need
                // the opposite, i.e. an index counted from the end.  However,
                // the Text node does not support wrapped line layout in the
                // reverse direction, starting at the bottom right corner.

                // We'll simulate by assuming the index will be a similar
                // number, then back up 10 characters just to add some slop.
                // For example, the ending lines might pack tighter than the
                // beginning lines, and therefore fit a higher number of
                // characters.
                int ind = Math.max(0, len - hit - 10);
                if (ind > 0 && wordTrim) {
                    int brInd = lastBreakCharIndex(text, ind + 1);
                    if (brInd >= 0) {
                        ind = brInd + 1;
                    } else {
                        brInd = firstBreakCharIndex(text, ind);
                        if (brInd >= 0) {
                            ind = brInd + 1;
                        }
                    }
                }
                if (center) {
                    // This is for the second part, i.e. from ellipsis to end of text.
                    result = result + text.substring(ind);
                } else {
                    result = ellipsis + text.substring(ind);
                }
            }

            // Step two, check if text still overflows after we added the ellipsis.
            // If so, remove one char or word at a time.
            while (true) {
                helper.setText(result);
                int hit2 = helper.impl_hitTestChar(endPoint).getCharIndex();
                if (center && hit2 < centerLen) {
                    // No room for text after ellipsis. Maybe there is a newline
                    // here, and the next line falls outside the view.
                    if (hit2 > 0 && result.charAt(hit2-1) == '\n') {
                        hit2--;
                    }
                    result = text.substring(0, hit2) + ellipsis;
                    break;
                } else if (hit2 > 0 && hit2 < result.length()) {
                    if (leading) {
                        int ind = eLen + 1; // Past ellipsis and first char.
                        if (wordTrim) {
                            int brInd = firstBreakCharIndex(result, ind);
                            if (brInd >= 0) {
                                ind = brInd + 1;
                            }
                        }
                        result = ellipsis + result.substring(ind);
                    } else if (center) {
                        int ind = centerLen + 1; // Past ellipsis and first char.
                        if (wordTrim) {
                            int brInd = firstBreakCharIndex(result, ind);
                            if (brInd >= 0) {
                                ind = brInd + 1;
                            }
                        }
                        result = result.substring(0, centerLen) + result.substring(ind);
                    } else {
                        int ind = result.length() - eLen - 1; // Before last char and ellipsis.
                        if (wordTrim) {
                            int brInd = lastBreakCharIndex(result, ind);
                            if (brInd >= 0) {
                                ind = brInd;
                            }
                        }
                        result = result.substring(0, ind) + ellipsis;
                    }
                } else {
                    break;
                }
            }
        }
        return result;
    }


    public static int firstBreakCharIndex(String str, int start) {
        char[] chars = str.toCharArray();
        for (int i = start; i < chars.length; i++) {
            if (isPreferredBreakCharacter(chars[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int lastBreakCharIndex(String str, int start) {
        char[] chars = str.toCharArray();
        for (int i = start; i >= 0; i--) {
            if (isPreferredBreakCharacter(chars[i])) {
                return i;
            }
        }
        return -1;
    }

    /** Recognizes white space and latin punctuation as preferred
     * line break positions. Could do a bit better with using more
     * of the properties from the Character class.
     */
    public static boolean isPreferredBreakCharacter(char ch) {
        if (Character.isWhitespace(ch)) {
            return true;
        } else {
            switch (ch) {
                case ';' :
                case ':' :
                case '.' :
                    return true;
                default: return false;
            }
        }
    }

    public static boolean requiresComplexLayout(Font font, String string) {
        return false;
    }

    public static int computeStartOfWord(Font font, String text, int index) {
        if ("".equals(text) || index < 0) return 0;
        if (text.length() <= index) return text.length();
        // if the given index is not in a word (but in whitespace), then
        // simply return the index
        if (Character.isWhitespace(text.charAt(index))) {
            return index;
        }
        boolean complexLayout = requiresComplexLayout(font, text);
        if (complexLayout) {
            // TODO needs implementation
            return 0;
        } else {
            // just start walking backwards from index until either i<0 or
            // the first whitespace is found.
            int i = index;
            while (--i >= 0) {
                if (Character.isWhitespace(text.charAt(i))) {
                    return i + 1;
                }
            }
            return 0;
        }
    }

    public static int computeEndOfWord(Font font, String text, int index) {
        if (text.equals("") || index < 0) {
            return 0;
        }
        if (text.length() <= index) {
            return text.length();
        }
        // if the given index is not in a word (but in whitespace), then
        // simply return the index
        if (Character.isWhitespace(text.charAt(index))) {
            return index;
        }
        boolean complexLayout = requiresComplexLayout(font, text);
        if (complexLayout) {
            // TODO needs implementation
            return text.length();
        } else {
            // just start walking forward from index until either i > length or
            // the first whitespace is found.
            int i = index;
            while (++i < text.length()) {
                if (Character.isWhitespace(text.charAt(i))) {
                    return i;
                }
            }
            return text.length();
        }
    }

}
