/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.visualize.draw;

import edu.caltech.ipac.astro.CoordException;
import edu.caltech.ipac.astro.target.TargetUtil;
import edu.caltech.ipac.firefly.visualize.Band;
import edu.caltech.ipac.visualize.plot.ActiveFitsReadGroup;
import edu.caltech.ipac.visualize.plot.CoordinateSys;
import edu.caltech.ipac.visualize.plot.ImageHeader;
import edu.caltech.ipac.visualize.plot.ImagePlot;
import edu.caltech.ipac.visualize.plot.ImagePt;
import edu.caltech.ipac.visualize.plot.ImageWorkSpacePt;
import edu.caltech.ipac.visualize.plot.PixelValueException;
import edu.caltech.ipac.visualize.plot.Plot;
import edu.caltech.ipac.visualize.plot.ProjectionException;
import edu.caltech.ipac.visualize.plot.Pt;
import edu.caltech.ipac.visualize.plot.WorldPt;
import edu.caltech.ipac.visualize.plot.plotdata.FitsRead;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.HashMap;


/**
 * Dialog for Flux Statistics
 * @author Tatiana Goldina
 * Moved from spot-common AreaStatisticsDialog
 */
public class AreaStatisticsUtil {
    public enum WhichDir {LON, LAT }
    public enum ReadoutMode {HMS, DECIMAL }
    private static final NumberFormat  _nf   = NumberFormat.getInstance();// OK for i18n

    public enum AreaBand {
        RED(Band.RED, "red"),
        GREEN(Band.GREEN, "green"),
        BLUE(Band.BLUE, "blue"),
        NO_BAND(Band.NO_BAND, "") ;

        AreaBand(Band band, String label) {
            _band = band;
            _label = label;
        }
        Band _band;
        String _label;
    }


    // degrees to readians conversion factor
    private static final double DtoR = Math.PI/180.0;

    // min, max, centroid, and flux weighted centroid
    // will be displayed as a number of JTextPanes
    // positioned using 4x3 GridLayout


    public static String formatPosHtml(Plot plot, ImageWorkSpacePt ip) {
        return formatReadoutByImagePt(plot, ip, "<br>");
    }


    public static String formatReadoutByImagePt(Plot plot, ImageWorkSpacePt ipt, String separator) {
        String retval;
        try {
            Point2D screenPt = null;
            ReadoutMode readoutMode = ReadoutMode.HMS;
            CoordinateSys coordSys= CoordinateSys.EQ_J2000;
            if (coordSys.equals(CoordinateSys.SCREEN_PIXEL)) {
                screenPt = plot.getScreenCoords(ipt);
            }
	    ImagePt ip = new ImagePt(ipt.getX(), ipt.getY());
            retval = getReadoutByImagePt(plot, ip, screenPt, WhichDir.LON, readoutMode, coordSys) +
                     separator +
                     getReadoutByImagePt(plot, ip, screenPt, WhichDir.LAT, readoutMode, coordSys);
            return retval;
        } catch (Exception e) {
            return "";
        }
    }

    private static String getReadoutByImagePt(Plot plot,
                                       ImagePt ip,
                                       Point2D screenPt,
                                       WhichDir dir,
                                       ReadoutMode mode,
                                       CoordinateSys coordSys) {
        String retStr;
        if (plot == null) {
            retStr= "";
        }
        else if (coordSys.equals(CoordinateSys.PIXEL)) {
            retStr= getDecimalXY(getValue(ip,dir)-0.5 , dir, coordSys);
        }
        else if (coordSys.equals(CoordinateSys.SCREEN_PIXEL)) {
            retStr= getDecimalXY(getValue(screenPt,dir), dir,
                                  CoordinateSys.SCREEN_PIXEL);
        }
        else {
            try {
		ImageWorkSpacePt iwspt = new ImageWorkSpacePt(ip.getX(), ip.getY());
                WorldPt degPt= plot.getWorldCoords(iwspt, coordSys);
                if (mode == ReadoutMode.HMS) {
                    retStr= getHmsXY(getValue(degPt,dir), dir, coordSys);
                }
                else if (mode == ReadoutMode.DECIMAL) {
                    retStr= getDecimalXY(getValue(degPt,dir), dir, coordSys);
                }
                else {
                    retStr= null;
                }
            } catch (ProjectionException pe) {
                retStr= "";
            }
        }
        return retStr;
    }

    private static String getDecimalXY(double val,
                                WhichDir dir,
                                CoordinateSys coordSys) {

        String desc= null;
        if (dir==WhichDir.LON) {
            desc= coordSys.getlonShortDesc();
        }
        else if (dir==WhichDir.LAT) {
            desc= coordSys.getlatShortDesc();
        }
        return desc + _nf.format(val);
    }

    private static String getHmsXY(double val, WhichDir which, CoordinateSys coordSys) {
        String retStr;
        try {
            if (which==WhichDir.LON) {
                retStr= coordSys.getlonShortDesc() +
                        TargetUtil.convertLonToString(val, coordSys.isEquatorial());
            }
            else if (which==WhichDir.LAT) {
                retStr= coordSys.getlatShortDesc() +
                        TargetUtil.convertLatToString(val, coordSys.isEquatorial());
            }
            else {
                retStr= null;
            }
        } catch (CoordException ce) {
            retStr= "";
        }
        return retStr;
    }



    private static double getValue(Point2D pt, WhichDir dir) {
        double val;
        if (dir==WhichDir.LON) {
            val= pt.getX();
        }
        else if (dir==WhichDir.LAT) {
            val= pt.getY();
        }
        else {
            val= 0;
        }
        return val;
    }

    private static double getValue(Pt pt, WhichDir dir) {
        double val;
        if (dir==WhichDir.LON) {
            val= pt.getX();
        }
        else if (dir==WhichDir.LAT) {
            val= pt.getY();
        }
        else {
            val= 0;
        }
        return val;
    }





    /**
     * The integrated flux (total flux from the area has different units from the flux
     * For example, if the flux unit (BUNIT in the header is "JY/SR" (Jansky per steradian),
     * then the integrated flux is JY.
     * This method converts flux units into integrated flux units.
     * @param fluxUnits flux units
     * @return integrated flux units
     */
    private static String getIntegratedFluxUnits(String fluxUnits) {
        final String [] match = {"/STER", "-SR", "/SR", "/ST", "*SR-1", "*ST-1"};
        String fluxUnitsUpper = fluxUnits.toUpperCase();
        int idx = -1;
        if (fluxUnitsUpper.indexOf("S") > 0) {
            for (String s : match) {
                idx = fluxUnitsUpper.lastIndexOf(s);
                if (idx > 0)
                    break;
            }
        }
        if (idx > 0) {
            return fluxUnits.substring(0, idx);
        } else {
            return fluxUnits.trim()+"*STER";
        }
    }







    private static boolean rotatedEllipseContains(Ellipse2D.Double shape, double x, double y, double rAngle) {
        double nAngle = Math.PI*2 - rAngle;
        double x_c = shape.getX() + shape.getWidth()/2;
        double y_c = shape.getY() + shape.getHeight()/2;
        double a = shape.getWidth()/2;
        double b = shape.getHeight()/2;


        double xdist = Math.cos(nAngle) * (x - x_c) - Math.sin(nAngle) * (y - y_c);
        double ydist = Math.sin(nAngle) * (x - x_c) + Math.cos(nAngle) * (y - y_c);

        double r = ((xdist*xdist/(a*a)) + (ydist*ydist/(b*b)));

        return (r <= 1);
    }

    /**
     * Get hash map with Area Statistics Metrics
     * @param plot  image plot
     * @param selectedBand selected color band, one of (ImagePlot.NO_BAND, ImagePlot.RED, ImagePlot.BLUE, ImagePlot.GREEN)
     * @param shape shape that contains region of calculation
     * @param boundingBox if not null, region of calculation will be intersection of shape and boundingBox
     * @return map of calculated metrics by metric id
     */
    public static HashMap<Metrics, Metric> getStatisticMetrics(ImagePlot plot, ActiveFitsReadGroup frGroup,
                                                               Band selectedBand, Shape shape,
                                                               Rectangle2D boundingBox, double rAngle) {

        //comment    
        if (boundingBox == null) boundingBox = shape.getBounds2D();
        double minX = boundingBox.getMinX();
        double maxX = boundingBox.getMaxX();
        double minY = boundingBox.getMinY();
        double maxY = boundingBox.getMaxY();

        double minimumX = 0d;
        double minimumY = 0d;
        double minimumFlux = Double.MAX_VALUE;
        double maximumX = 0d;
        double maximumY = 0d;
        double maximumFlux = Double.MIN_VALUE;
        double flux;
        double nPixels = 0d;
        double fluxSum = 0d;
        double squareSum = 0d;
        double xfSum = 0d;
        double yfSum = 0d;
        double xSum = 0d;
        double ySum = 0d;
        FitsRead fitsRead = frGroup.getFitsRead(selectedBand);
        int imageScaleFactor = fitsRead.getImageScaleFactor();
        if (imageScaleFactor < 1) imageScaleFactor = 1;
        // to calculate the number of pixels (and integratedFlux) correctly in overlay plots,



        /* adjust minX, minY, maxX, maxY to be centered on a pixel */
        ImageWorkSpacePt iwwspt = new ImageWorkSpacePt(minX,minY);
        ImagePt ippt = plot.getImageCoords(iwwspt);
        ippt = new ImagePt(
                Math.rint(ippt.getX() + 0.5) -0.5,
                Math.rint(ippt.getY() + 0.5) -0.5);
        iwwspt = plot.getImageCoords(ippt);
        minX = iwwspt.getX();
        minY = iwwspt.getY();

        iwwspt = new ImageWorkSpacePt(maxX,maxY);
        ippt = plot.getImageCoords(iwwspt);
        ippt = new ImagePt(
                Math.rint(ippt.getX() + 0.5) -0.5,
                Math.rint(ippt.getY() + 0.5) -0.5);
        iwwspt = plot.getImageCoords(ippt);
        maxX = iwwspt.getX();
        maxY = iwwspt.getY();

        // I need to take into account imageScaleFactor from FitsRead
        for (double y=minY; y<=maxY; y+=imageScaleFactor ) {
            for (double x=minX; x<=maxX; x+=imageScaleFactor) {
                try {
                    if ((rAngle != 0.0 && !rotatedEllipseContains((Ellipse2D.Double)shape, x, y, rAngle)) ||
                        (rAngle == 0.0 && !shape.contains(x, y)))
                        continue;
                    if (selectedBand == Band.NO_BAND) {
                        flux = plot.getFlux(new ImageWorkSpacePt(x, y),frGroup);
                    } else {
                        flux = plot.getFlux(frGroup, selectedBand, new ImageWorkSpacePt(x, y));
                    }
                    if (Double.isNaN(flux))
                    {
                        continue;
                    }
                    nPixels++;
                    fluxSum += flux;
                    squareSum += flux*flux;
                    xfSum += x*flux;
                    yfSum += y*flux;
                    xSum += x;
                    ySum += y;

                    if (flux < minimumFlux) {
                        minimumFlux = flux;
                        minimumX = x;
                        minimumY = y;
                    }
                    if (flux > maximumFlux) {
                        maximumFlux = flux;
                        maximumX = x;
                        maximumY = y;
                    }
                } catch (PixelValueException pve) {
                    // do nothing
//                    System.out.println("Pixel Value Exception: ("+x+","+y+") "+pve.getMessage());
                }
            }
        }
        if (nPixels == 0) {
            HashMap<Metrics, Metric> metrics = new HashMap<Metrics, Metric>(1);
            metrics.put(Metrics.NUM_PIXELS, new Metric("Number Of Pixels", null, 0, ""));
            return metrics;
        }

        String fluxUnits = ((selectedBand == Band.NO_BAND) ? plot.getFluxUnits(frGroup) : plot.getFluxUnits(selectedBand,frGroup));


        // calculate the centroid position
        double xCentroid = xSum / nPixels;
        double yCentroid = ySum / nPixels;
        double xfCentroid = xfSum / fluxSum;
        double yfCentroid = yfSum / fluxSum;

        // calculate the mean and standard deviation
        double averageFlux = fluxSum/nPixels;
        double stdev = Math.sqrt(squareSum/nPixels - averageFlux*averageFlux);

	// calculate integrated flux
//	ImageHeader imageHdr = fitsRead.getImageHeader();

        ImageHeader imageHdr = new ImageHeader(fitsRead.getHeader());

        // pixel area in steradian
	double pixelArea =  Math.abs(imageHdr.cdelt1*DtoR*imageHdr.cdelt2*DtoR);
	//double integratedFlux = nPixels*pixelArea*fluxSum;
	// Schuyler: avoid double counting the number of pixels, which is already implicitly included in fluxSum
	double integratedFlux = pixelArea*fluxSum;
	String integratedFluxUnits = getIntegratedFluxUnits(fluxUnits);

	if (fluxUnits.equals("mag"))
	{
	    /* exchange max and min */
	    double temp;
	    temp = minimumFlux;
	    minimumFlux = maximumFlux;
	    maximumFlux = temp;
	    temp = minimumX;
	    minimumX = maximumX;
	    maximumX = temp;
	    temp = minimumY;
	    minimumY = maximumY;
	    maximumY = temp;

	    // calculate integrated flux
	    integratedFlux = fluxSum;
	    integratedFluxUnits = "mag";
	}

        HashMap<Metrics, Metric> metrics = new HashMap<Metrics, Metric>(Metrics.values().length);

        ImageWorkSpacePt maxIp = new ImageWorkSpacePt(maximumX, maximumY);
        metrics.put(Metrics.MAX, new Metric("Maximum Flux", maxIp, maximumFlux, fluxUnits));
        ImageWorkSpacePt minIp = new ImageWorkSpacePt(minimumX, minimumY);
        metrics.put(Metrics.MIN, new Metric("Minimum Flux", minIp, minimumFlux, fluxUnits));
        ImageWorkSpacePt centroidIp = new ImageWorkSpacePt(xCentroid, yCentroid);
        metrics.put(Metrics.CENTROID, new Metric("Aperture Centroid", centroidIp, Metric.NULL_DOUBLE, null));
        ImageWorkSpacePt fwCentroidIp = new ImageWorkSpacePt(xfCentroid, yfCentroid);
        metrics.put(Metrics.FW_CENTROID, new Metric("Flux Weighted Centroid", fwCentroidIp, Metric.NULL_DOUBLE, null));

        metrics.put(Metrics.MEAN, new Metric("Mean Flux", null, averageFlux, fluxUnits));
        metrics.put(Metrics.STDEV, new Metric("Standard Deviation", null, stdev, fluxUnits));
        metrics.put(Metrics.INTEGRATED_FLUX,  new Metric("Integrated Flux", null, integratedFlux, integratedFluxUnits));

        metrics.put(Metrics.NUM_PIXELS,  new Metric("Number of Pixels", null, nPixels, ""));
        metrics.put(Metrics.PIXEL_AREA, new Metric("Pixel Area", null, pixelArea, "STER"));

        return metrics;
    }

}

