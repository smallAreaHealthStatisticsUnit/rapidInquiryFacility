package rifServices.dataStorageLayer.common;
 
import org.jfree.chart.renderer.category.StackedBarRenderer;
import java.awt.Color;
import java.awt.Paint;
 
 /**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Peter Hambly
 * @author phambly
 */

 /**
 * A custom renderer that returns a different color from a set for each item in a two distinct series.
 * Used to vary the colors slightly in the RIF population pyramid
 * https://stackoverflow.com/questions/4952931/jfreechart-is-it-possible-to-change-the-bar-color
 * http://www.java2s.com/Code/Java/Chart/JFreeChartBarChartDemo3differentcolorswithinaseries.htm
 */
class RifPopulationStackedBarRenderer extends StackedBarRenderer {

    /** The colors. */
    private Paint[] maleColors;
    private Paint[] femaleColors;

    /**
     * Creates a new renderer.
     *
     * @param maleColors  the colors.
     * @param femaleColors  the colors.
     */
    public RifPopulationStackedBarRenderer(final Paint[] maleColors, final Paint[] femaleColors) {
        this.maleColors = maleColors;
        this.femaleColors = femaleColors;
    }

    /**
     * Returns the paint for an item.  Overrides the default behaviour inherited from
     * AbstractSeriesRenderer.
     *
     * @param row  the series.
     * @param column  the category.
     *
     * @return The item color.
     */
    public Paint getItemPaint(final int row, final int column) throws IllegalArgumentException {
		if (row == 0) { // Males
			return this.maleColors[column % this.maleColors.length];
		}
		else if (row == 1) { // Females
			return this.femaleColors[column % this.femaleColors.length];
		}
		else {
			throw new IllegalArgumentException("Unable to get color for RIF population pyramid; using > 2 bar stacks for row: " + (row+1));
		}
    }
}