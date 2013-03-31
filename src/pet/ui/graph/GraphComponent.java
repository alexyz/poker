package pet.ui.graph;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

/** displays data in a graph */
public class GraphComponent extends JComponent {
	
	private GraphData data;
	private int modelminx, modelminy, modelmaxx, modelmaxy;
	private String xdesc;
	
	public GraphComponent() {
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				//System.out.println("mouse moved");
				if (data != null) {
					final int w = getWidth();
					final int xm = w / 10;
					final int gw = w - xm * 2;
					int mx = getMX(gw, xm, e.getX());
					xdesc = data.getXDesc(mx);
					repaint();
				} else {
					xdesc = null;
				}
			}
		});
	}
	
	public void setData(GraphData data) {
		this.data = data;
		System.out.println("data points: " + data.pointsMap.size());
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (List<GraphDataPoint> l : data.pointsMap.values()) {
			for (GraphDataPoint d : l) {
				int x = d.x;
				if (x > maxx) {
					maxx = x;
				}
				if (x < minx) {
					minx = x;
				}
				int y = d.y;
				if (y > maxy) {
					maxy = y;
				}
				if (y < miny) {
					miny = y;
				}
			}
		}
		System.out.println(String.format("x: %d to %d, y: %d to %d", minx, maxx, miny, maxy));
		this.modelminx = minx;
		this.modelminy = miny;
		this.modelmaxx = maxx;
		this.modelmaxy = maxy;
	}
	
	@Override
	protected void paintComponent(Graphics g_) {
		Graphics2D g = (Graphics2D) g_;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (data == null)
			return;
		
		// component size
		final int w = getWidth();
		final int h = getHeight();
		
		// XXX should filter points list
		// because there's not much point plotting 10k points on a 500px component
		
		// margin
		final int xmargin = w / 10;
		final int ymargin = h / 10;
		// graph size
		final int graphwidth = w - xmargin * 2;
		final int graphheight = h - ymargin * 2;
		// graph corners
		final int viewminx = getVX(graphwidth, xmargin, modelminx);
		final int viewminy = getVY(graphheight, ymargin, modelminy);
		final int viewmaxx = getVX(graphwidth, xmargin, modelmaxx);
		final int viewmaxy = getVY(graphheight, ymargin, modelmaxy);
		
		// title
		if (data.name != null) {
			g.setColor(Color.black);
			final int sw = g.getFontMetrics().stringWidth(data.name);
			g.drawString(data.name, (w - sw) / 2, h / 20);
		}

		// x axis
		g.setColor(Color.red);
		g.drawLine(viewminx, viewminy, viewmaxx, viewminy);
		for (int xx = 0; xx <= 10; xx++) {
			final int mx = modelminx + ((modelmaxx - modelminx) * xx) / 10;
			final int vx = getVX(graphwidth, xmargin, mx);
			
			g.drawLine(vx, viewminy, vx, viewminy + 5);
			String n = data.getXName(mx);
			final int sw = g.getFontMetrics().stringWidth(n);
			final int sh = g.getFontMetrics().getHeight();
			g.drawString(n, vx - (sw / 2), viewminy + sh + 2);
		}
		
		// x label
		final String xlabel = data.x + (xdesc != null ? " - " + xdesc : "");
		final int xlabelwidth = g.getFontMetrics().stringWidth(xlabel);
		g.drawString(xlabel, ((viewmaxx - viewminx - xlabelwidth) / 2) + viewminx, h - (ymargin / 2));
		
		// y axis
		g.drawLine(viewminx, viewminy, viewminx, viewmaxy);
		for (int yy = 0; yy <= 10; yy++) {
			final int my = modelminy + ((modelmaxy - modelminy) * yy) / 10;
			final int vy = getVY(graphheight, ymargin, my);
			
			g.drawLine(viewminx, vy, viewminx - 5, vy);
			String n = data.getYName(my);
			final int sw = g.getFontMetrics().stringWidth(n);
			final int sh = g.getFontMetrics().getHeight();
			g.drawString(n, viewminx - sw - 10, vy + (sh / 2) - 3);
		}

		// data
		for (List<GraphDataPoint> points : data.pointsMap.values()) {
			GraphDataPoint prevd = points.get(0);
			for (int n = 1; n < points.size(); n++) {
				final GraphDataPoint d = points.get(n);
				final int prevvx = getVX(graphwidth, xmargin, prevd.x);
				final int prevvy = getVY(graphheight, ymargin, prevd.y);
				final int vx = getVX(graphwidth, xmargin, d.x);
				final int vy = getVY(graphheight, ymargin, d.y);
				if (d.div) {
					g.setColor(Color.white);
					g.drawLine(vx, viewminy, vx, viewmaxy);
				}
				g.setColor(Color.black);
				g.drawLine(prevvx, prevvy, vx, vy);
				prevd = d;
			}
		}
		
		// y label
		{
			Graphics2D g2 = (Graphics2D) g.create();
			final int ylw = g2.getFontMetrics().stringWidth(data.y);
			final int ylvx = viewminx - (xmargin / 2);
			final int ylvy = ((viewmaxy - viewminy - ylw) / 2) + viewminy;
			// translate before rotate...
			g2.translate(ylvx, ylvy);
			g2.setColor(Color.red);
			g2.rotate(Math.PI * 0.5);
			g2.drawString(data.y, 0, 0);
			g2.dispose();
		}
	}

	/** get view x for graph width, x margin and model x */
	private int getVX(int graphwidth, int xmargin, int modelx) {
		final int x1 = modelx - modelminx;
		final int x2 = x1 * graphwidth;
		final int x3 = x2 / Math.max(modelmaxx - modelminx, 1);
		final int x4 = x3 + xmargin;
		return x4;
	}
	
	private int getMX(int graphwidth, int xmargin, int viewx) {
		final int y1 = viewx - xmargin;
		final int y2 = y1 * Math.max(modelmaxx - modelminx, 1);
		final int y3 = y2 / graphwidth;
		final int y4 = y3 + modelminx;
		return y4;
	}
	
	/** get view y for graph height and y margin */
	private int getVY(int graphheight, int ymargin, int modely) {
		return (graphheight - (((modely - modelminy) * graphheight) / Math.max(modelmaxy - modelminy, 1))) + ymargin;
	}

}
