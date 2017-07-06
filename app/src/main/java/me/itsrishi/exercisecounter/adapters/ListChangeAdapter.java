/*
 * MIT License
 *
 * Copyright (c) 2017 Rishi Raj
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.itsrishi.exercisecounter.adapters;

/**
 * @author Rishi Raj
 */

public interface ListChangeAdapter {

    /**
     * Calls all the listeners to make changes for the movement of required item
     *
     * @param fromPosition
     * @param toPosition
     * @return If the clickListener was able to successfully make changes required for this move.
     * Never return false from a silent observer as it will abort the whole operation
     */
    public boolean onItemMove(int fromPosition, int toPosition);

    /**
     * Calls all the listeners to make changes for the dismissal of required item
     *
     * @param position The position of item to be dismissed
     * @return If the clickListener was able to successfully make changes required for this dismissal.
     * Never return false from a silent observer as it will abort the whole operation
     */
    public boolean onItemDismiss(int position);
}
