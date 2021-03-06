/*  
 * Copyright (c) 2004-2013 Regents of the University of California.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3.  Neither the name of the University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * Copyright (c) 2014 Martin Stockhammer
 */
package prefux.action.layout;

import java.util.Iterator;

import prefux.visual.VisualItem;


/**
 * Layout Action that sets x, y coordinates for a visual item by simply
 * looking them up from another data field.
 *  
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class SpecifiedLayout extends Layout {

    private String m_xfield = null;
    private String m_yfield = null;
    private String m_fixedfield = null;
    
    /**
     * Create a new SpecifiedLayout.
     * @param group the data group to layout
     * @param xField the field from which to lookup x coordinate values
     * @param yField the field from which to lookup y coordinate values
     */
    public SpecifiedLayout(String group, String xField, String yField) {
        super(group);
        m_xfield = xField;
        m_yfield = yField;
    }

    // ------------------------------------------------------------------------

    /**
     * Get the field to lookup to set the x-coordinate.
     * @return the x-value field. If null, this action
     * does not set the x-coordiante.
     */
    public String getXField() {
        return m_xfield;
    }

    /**
     * Set the field to lookup to set the x-coordinate.
     * @param xField the x-value field to use. If null, this action
     * will not set the x-coordiante.
     */
    public void setXField(String xField) {
        m_xfield = xField;
    }

    /**
     * Get the field to lookup to set the y-coordinate.
     * @return the y-value field. If null, this action
     * does not set the y-coordiante.
     */
    public String getYField() {
        return m_yfield;
    }

    /**
     * Set the field to lookup to set the y-coordinate.
     * @param yField the y-value field to use. If null, this action
     * will not set the y-coordiante.
     */
    public void setYField(String yField) {
        m_yfield = yField;
    }
    
    /**
     * Get the field to lookup to set the fixed property.
     * @return the fixed field. If null, this action
     * does not set the fixed field.
     */
    public String getFixedField() {
        return m_fixedfield;
    }

    /**
     * Set the field to lookup to set the fixed property.
     * @param fixedField the fixed field to use. If null, this action
     * will not set the fixed field.
     */
    public void setFixedField(String fixedField) {
        m_fixedfield = fixedField;
    }

    /**
     * @see prefux.action.Action#run(double)
     */
    public void run(double frac) {
        Iterator iter = m_vis.items(m_group);
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            try {
                if ( m_xfield != null )
                    setX(item, null, item.getDouble(m_xfield));
                if ( m_yfield != null )
                    setY(item, null, item.getDouble(m_yfield));
                if ( m_fixedfield != null )
                    item.setFixed(item.getBoolean(m_fixedfield));
            } catch ( Exception e ) {
            }
        }
    }

} // end of class SpecifiedLayout
