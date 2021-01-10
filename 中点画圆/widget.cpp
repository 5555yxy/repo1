//2016cyy Cprograming
#include "widget.h"
#include<math.h>
#define H 1080
#define W 1920
Widget::Widget(QWidget *parent) :
	QWidget(parent)
{
}

Widget::~Widget()
{
}

void Widget::mouseMoveEvent(QMouseEvent *event) {
	endPt = event->pos();
	update();
}
void Widget::mouseReleaseEvent(QMouseEvent *event) {
	endPt = event->pos();
	update();
}
void Widget::mousePressEvent(QMouseEvent *event) {
	startPt = event->pos();
}

//qpoint(image,x,y)
void qpoint(QImage &image, int x, int y) {
	if (x<1 || x>W - 1 || y<1 || y>H - 1) {
		;
	}
	else {
		unsigned char *ptrRow = image.scanLine(y);
		ptrRow[x * 3 + 0] = 0;
		ptrRow[x * 3 + 1] = 255;
		ptrRow[x * 3 + 2] = 0;
	}
}

void drawLine(QImage &image, QPoint pt1, QPoint pt2) {
	//    QPainter painter(&image);
	//    painter.drawLine(pt1,pt2);
	QPainter painter(&image);
	int flag = -1;//1为扫描x，2为扫描y
	int x = 0;
	int y = 0;
	int p1x = pt1.x();
	int p1y = pt1.y();
	int p2x = pt2.x();
	int p2y = pt2.y();
	int a, b;
	int d, d1;
	a = p2x - p1x;
	b = p2y - p1y;
	/*
						|
						|
				  21  	|	 22							|
						|								|b
			12			|			11			________|
	____________________|_______________________	a
						|
			11			|			12
						|
				  22  	|	 21
						|
						|
	*/
	if (a >= 0 && b <= 0 && a >= -b || a <= 0 && b >= 0 && -a >= b) {
		printf("11\n");
		a = p2x - p1x;
		b = p1y - p2y;
		d = b - 0.5*a;
		d1 = b - a;
		flag = 11;
	}
	else if (a > 0 && b > 0 && a >= b || a < 0 && b < 0 && -a > -b) {
		printf("12\n");
		a = p2x - p1x;
		b = p2y - p1y;
		d = b - 0.5*a;
		d1 = b - a;
		flag = 12;
	}
	else if (b >= 0 && a >= 0 && a < b || b <= 0 && a <= 0 && -a < -b) {
		b = p1x - p2x;
		a = p1y - p2y;
		d = b - 0.5*a;
		d1 = b - a;
		flag = 21;
	}
	else if (b < 0 && a > 0 && a < -b || b > 0 && a < 0 && -a < b) {
		b = p2x - p1x;
		a = p1y - p2y;
		d = b - 0.5*a;
		d1 = b - a;
		flag = 22;
	}
	if (flag == 11) {
		if (p1x < p2x) {
			for (x = p1x, y = p1y; x < p2x; x++) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0) {
					d += d1;
					--y;
				}
				else {
					d += b;
				}
			}
		}
		else {
			for (x = p1x, y = p1y; x > p2x; x--) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0) {
					d += d1;
					++y;
				}
				else {
					d += b;
				}
			}
		}
	}
	else if (flag == 12) {
		if (p1x < p2x) {
			for (x = p1x, y = p1y; x < p2x; x++) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0) {
					d += d1;
					++y;
				}
				else {
					d += b;
				}
			}
		}
		else {
			for (x = p1x, y = p1y; x > p2x; x--) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0) {
					d += d1;
					--y;
				}
				else {
					d += b;
				}
			}
		}
	}
	else if (flag == 21) {
		if (p1y < p2y) {
			for (x = p1x, y = p1y; y < p2y; y++) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0) {
					d += d1;
					++x;
				}
				else {
					d += b;
				}
			}
		}
		else {
			for (x = p1x, y = p1y; y > p2y; y--) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0) {
					d += d1;
					--x;
				}
				else {
					d += b;
				}
			}
		}
	}
	else if (flag == 22) {
		if (p1y < p2y) {
			for (x = p1x, y = p1y; y < p2y; y++) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0) {
					d += d1;
					--x;
				}
				else {
					d += b;
				}
			}
		}
		else {
			for (x = p1x, y = p1y; y > p2y; y--) {
				if (x<1 || x>W - 1 || y<1 || y>H - 1) {
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0) {
					d += d1;
					++x;
				}
				else {
					d += b;
				}
			}
		}
	}
}

void mid_point_circle(QImage &image, QPoint pt1, QPoint pt2) {
	int x = 0;
	double r;
	r = (pt2.x() - pt1.x())*(pt2.x() - pt1.x()) + (pt2.y() - pt1.y())*(pt2.y() - pt1.y());
	r = sqrt(r);
	int y = r;
	double d = 1.25 - r;
	qpoint(image, pt1.x(), y + pt1.y());
	qpoint(image, pt1.x() + y, pt1.y());
	qpoint(image, pt1.x(), pt1.y() - y);
	qpoint(image, pt1.x() - y, pt1.y());

	while (x < y) {
		if (d < 0) {
			d += 2 * x + 3;
		}
		else {
			d += 2 * (x - y) + 5;
			y--;
		}
		x++;
		qpoint(image, x + pt1.x(), y + pt1.y());
		qpoint(image, -x + pt1.x(), -y + pt1.y());
		qpoint(image, y + pt1.x(), x + pt1.y());
		qpoint(image, -y + pt1.x(), -x + pt1.y());
		qpoint(image, x + pt1.x(), -y + pt1.y());
		qpoint(image, -x + pt1.x(), y + pt1.y());
		qpoint(image, -y + pt1.x(), x + pt1.y());
		qpoint(image, y + pt1.x(), -x + pt1.y());
	}
}


void Widget::paintEvent(QPaintEvent *event) {
	QPainter painter(this);
	QImage image = QImage(W, H, QImage::Format_RGB888);
	image.fill(QColor(2, 2, 2));
	drawLine(image, startPt, endPt);
	mid_point_circle(image, startPt, endPt);
	painter.drawImage(0, 0, image);
}
