package pet.ui.gr;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/** displays data in a graph */
public class GraphComponent extends JComponent {
	
	private GraphData<?> data;
	private int minx, miny, maxx, maxy;
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
	
	public void setData(GraphData<?> data) {
		this.data = data;
		System.out.println("data points: " + data.points.size());
		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (GraphDataPoint<?> d : data.points) {
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
		System.out.println(String.format("x: %d to %d, y: %d to %d", minx, maxx, miny, maxy));
		this.minx = minx;
		this.miny = miny;
		this.maxx = maxx;
		this.maxy = maxy;
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
		
		if (data.points.size() > w) {
			// XXX should filter
		}
		
		// margin
		final int xm = w / 10;
		final int ym = h / 10;
		// graph size
		final int gw = w - xm * 2;
		final int gh = h - ym * 2;
		// graph corners
		final int vminx = getVX(gw, xm, minx);
		final int vminy = getVY(gh, ym, miny);
		final int vmaxx = getVX(gw, xm, maxx);
		final int vmaxy = getVY(gh, ym, maxy);
		
		// title
		if (data.name != null) {
			g.setColor(Color.black);
			final int sw = g.getFontMetrics().stringWidth(data.name);
			g.drawString(data.name, (w - sw) / 2, h / 20);
		}

		// x axis
		g.setColor(Color.red);
		g.drawLine(vminx, vminy, vmaxx, vminy);
		for (int xx = 0; xx <= 10; xx++) {
			final int mx = minx + ((maxx - minx) * xx) / 10;
			final int vx = getVX(gw, xm, mx);
			
			g.drawLine(vx, vminy, vx, vminy + 5);
			String n = data.getXName(mx);
			final int sw = g.getFontMetrics().stringWidth(n);
			final int sh = g.getFontMetrics().getHeight();
			g.drawString(n, vx - (sw / 2), vminy + sh + 2);
		}
		
		// x label
		final String xl = data.x + (xdesc != null ? " - " + xdesc : "");
		final int xlw = g.getFontMetrics().stringWidth(xl);
		g.drawString(xl, ((vmaxx - vminx - xlw) / 2) + vminx, h - (ym / 2));
		
		// y axis
		g.drawLine(vminx, vminy, vminx, vmaxy);
		for (int yy = 0; yy <= 10; yy++) {
			final int my = miny + ((maxy - miny) * yy) / 10;
			final int vy = getVY(gh, ym, my);
			
			g.drawLine(vminx, vy, vminx - 5, vy);
			String n = data.getYName(my);
			final int sw = g.getFontMetrics().stringWidth(n);
			final int sh = g.getFontMetrics().getHeight();
			g.drawString(n, vminx - sw - 10, vy + (sh / 2) - 3);
		}

		// data
		g.setColor(Color.black);
		GraphDataPoint<?> prevd = data.points.get(0);
		for (int n = 1; n < data.points.size(); n++) {
			final GraphDataPoint<?> d = data.points.get(n);
			final int prevvx = getVX(gw, xm, prevd.x);
			final int prevvy = getVY(gh, ym, prevd.y);
			final int vx = getVX(gw, xm, d.x);
			final int vy = getVY(gh, ym, d.y);
			g.drawLine(prevvx, prevvy, vx, vy);
			prevd = d;
		}
		
		// y label
		{
			Graphics2D g2 = (Graphics2D) g.create();
			final int ylw = g2.getFontMetrics().stringWidth(data.y);
			final int ylvx = vminx - (xm / 2);
			final int ylvy = ((vmaxy - vminy - ylw) / 2) + vminy;
			// translate before rotate...
			g2.translate(ylvx, ylvy);
			g2.setColor(Color.red);
			g2.rotate(Math.PI * 0.5);
			g2.drawString(data.y, 0, 0);
			g2.dispose();
		}
	}

	/** get view x for graph width, x margin and model x */
	private int getVX(int gw, int xm, int mx) {
		final int x1 = mx - minx;
		final int x2 = x1 * gw;
		final int x3 = x2 / Math.max(maxx - minx, 1);
		final int x4 = x3 + xm;
		return x4;
	}
	
	private int getMX(int gw, int xm, int vx) {
		final int y1 = vx - xm;
		final int y2 = y1 * Math.max(maxx - minx, 1);
		final int y3 = y2 / gw;
		final int y4 = y3 + minx;
		return y4;
	}
	
	/** get view y for graph height and y margin */
	private int getVY(int gh, int ym, int y) {
		return (gh - (((y - miny) * gh) / Math.max(maxy - miny, 1))) + ym;
	}

}
