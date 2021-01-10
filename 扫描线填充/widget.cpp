//2016CYY Cprogramming
#include "widget.h"
#include <QDebug>
#include <QVector>
#include <iostream>
using namespace std;
#define H 1080
#define W 1920
int click = 0; //端点数量
QPoint temp;
QPoint first;
int result = 1; //判断有没有结束
int sign = 1;   //2为画线
int length = 5;

struct edge
{
	int ymax;
	float x;
	float dx;
	edge *next;
};
edge edge_;
QVector<edge> edges[H];
QVector<QPoint> points;//填充用
bool fin = false;
QPoint *Queue = (QPoint *)malloc(length * sizeof(QPoint)); //存放端点的数组

Widget::Widget(QWidget *parent) : QWidget(parent)
{
}

Widget::~Widget()
{
}

void Widget::mouseMoveEvent(QMouseEvent *event)
{
	setMouseTracking(true);
	if (click > 0 && result != 0)
	{
		startPt = temp;
		endPt = event->pos();
		sign = 2;
		update();
	}
}
void Widget::mouseReleaseEvent(QMouseEvent *event)
{
	if (event->button() == Qt::LeftButton)
	{

	}
	else if (event->button() == Qt::RightButton)
	{
		sign = 2;
	}
}

void pscan_push()
{
	for (int k = 1; k < H; k++)
	{
		for (int i = 0; i < click - 1; i++)
		{
			if ((Queue[i].y() <= k && k < Queue[i + 1].y() && i != click) || (Queue[i].y() > k && k >= Queue[i + 1].y() && i != click))
			{
				if (Queue[i].y() >= Queue[i + 1].y())
				{
					edge_.ymax = Queue[i].y();
				}
				else
				{
					edge_.ymax = Queue[i + 1].y();
				}
				if ((Queue[i + 1].y() - Queue[i].y()) != 0)
				{
					edge_.dx = (float(Queue[i + 1].x()) - float(Queue[i].x())) / (float(Queue[i + 1].y()) - float(Queue[i].y()));
				}
				edge_.x = Queue[i].x() + (k - Queue[i].y()) * edge_.dx;
				edges[k].push_back(edge_);
			}
		}
	}

	for (int i = 0; i < H; i++)
	{
		for (int j = 0; j < edges[i].size() - 1; j++)
		{
			for (int k = 0; k < edges[i].size() - 1 - j; k++)
			{
				if (edges[i][k].x > edges[i][k + 1].x)
				{
					swap(edges[i][k].x, edges[i][k + 1].x);
				}
			}
		}
	}
	for (int i = 1; i < H; i++)
	{
		QPoint startPt;
		QPoint endPt;
		if (edges[i].size() != 0)
		{
			for (int j = 0; j < edges[i].size(); j = j + 2)
			{
				startPt.setY(i);
				startPt.setX(edges[i][j].x);
				endPt.setY(i);
				endPt.setX(edges[i][j + 1].x + 1);
				points.push_back(startPt);
				points.push_back(endPt);
			}
		}
	}
	fin = true;
}

void Widget::mousePressEvent(QMouseEvent * event)
{
	if (event->button() == Qt::RightButton)
	{
		//result = 1;
		setMouseTracking(true);
		if (click == 0 && result != 0)
		{
			for (int i = 0; i < H; i++)
			{
				edges[i].clear();
			}
			points.clear();
			startPt = event->pos();
			temp = startPt;
			first = startPt;
			Queue[click] = startPt;
			click++;
		}
		else if (result == 0)
		{
			fin = false;
			click = 0;
			result = 1;
			//sign = 1;
			update();
		}
		else
		{
			startPt = temp;
			endPt = event->pos();
			if (click >= length)
			{
				length += 5;
				Queue = (QPoint *)realloc(Queue, (length) * sizeof(QPoint));
			}
			Queue[click] = endPt;
			click++;
			sign = 2;
			update();
			temp = endPt;
		}
	}
	else if (event->button() == Qt::LeftButton)
	{
		if (click > 0)
		{

			startPt = temp;
			endPt = first;
			if (click >= length)
			{
				length += 5;
				Queue = (QPoint *)realloc(Queue, (length) * sizeof(QPoint));
			}
			Queue[click] = endPt;
			cout << click << "\n";
			click++;
			sign = 1;
			result = 0;
			update();
			temp = endPt;
			if (sign == 1)
			{
				pscan_push();
				update();
			}
		}
	}
	else if (event->button() == Qt::MidButton)
	{
		
	}
}

void qpoint(QImage & image, int x, int y)
{
	unsigned char *ptrRow = image.scanLine(y);
	ptrRow[x * 3 + 0] = 0;//64 201 176
	ptrRow[x * 3 + 1] = 190;
	ptrRow[x * 3 + 2] = 0;
}

void drawLine(QImage & image, QPoint pt1, QPoint pt2)
{
	QPainter painter(&image);
	int flag = -1; //1为扫描x，2为扫描y
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
			  21  	|	 22									|
					|										|b
		12			|			11					________|
____________________|_______________________			a
					|
		11			|			12
					|
			  22  	|	 21
					|
					|
*/
	if (a >= 0 && b <= 0 && a >= -b || a <= 0 && b >= 0 && -a >= b)
	{
		printf("11\n");
		a = p2x - p1x;
		b = p1y - p2y;
		d = b - 0.5 * a;
		d1 = b - a;
		flag = 11;
	}
	else if (a > 0 && b > 0 && a >= b || a < 0 && b < 0 && -a > -b)
	{
		printf("12\n");
		a = p2x - p1x;
		b = p2y - p1y;
		d = b - 0.5 * a;
		d1 = b - a;
		flag = 12;
	}
	else if (b >= 0 && a >= 0 && a < b || b <= 0 && a <= 0 && -a < -b)
	{
		b = p1x - p2x;
		a = p1y - p2y;
		d = b - 0.5 * a;
		d1 = b - a;
		flag = 21;
	}
	else if (b < 0 && a > 0 && a < -b || b > 0 && a < 0 && -a < b)
	{
		b = p2x - p1x;
		a = p1y - p2y;
		d = b - 0.5 * a;
		d1 = b - a;
		flag = 22;
	}
	if (flag == 11)
	{
		if (p1x < p2x)
		{
			for (x = p1x, y = p1y; x < p2x; x++)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0)
				{
					d += d1;
					--y;
				}
				else
				{
					d += b;
				}
			}
		}
		else
		{
			for (x = p1x, y = p1y; x > p2x; x--)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0)
				{
					d += d1;
					++y;
				}
				else
				{
					d += b;
				}
			}
		}
	}
	else if (flag == 12)
	{
		if (p1x < p2x)
		{
			for (x = p1x, y = p1y; x < p2x; x++)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0)
				{
					d += d1;
					++y;
				}
				else
				{
					d += b;
				}
			}
		}
		else
		{
			for (x = p1x, y = p1y; x > p2x; x--)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0)
				{
					d += d1;
					--y;
				}
				else
				{
					d += b;
				}
			}
		}
	}
	else if (flag == 21)
	{
		if (p1y < p2y)
		{
			for (x = p1x, y = p1y; y < p2y; y++)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0)
				{
					d += d1;
					++x;
				}
				else
				{
					d += b;
				}
			}
		}
		else
		{
			for (x = p1x, y = p1y; y > p2y; y--)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0)
				{
					d += d1;
					--x;
				}
				else
				{
					d += b;
				}
			}
		}
	}
	else if (flag == 22)
	{
		if (p1y < p2y)
		{
			for (x = p1x, y = p1y; y < p2y; y++)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d <= 0)
				{
					d += d1;
					--x;
				}
				else
				{
					d += b;
				}
			}
		}
		else
		{
			for (x = p1x, y = p1y; y > p2y; y--)
			{
				if (x < 1 || x > W - 1 || y < 1 || y > H - 1)
				{
					continue;
				}
				qpoint(image, x, y);
				if (d >= 0)
				{
					d += d1;
					++x;
				}
				else
				{
					d += b;
				}
			}
		}
	}
}

void Widget::paintEvent(QPaintEvent * event)
{
	QPainter painter(this);
	QImage image = QImage(W, H, QImage::Format_RGB888);
	image.fill(QColor(3, 3, 3));
	for (int i = 0; i < click - 1; i++)
	{
		drawLine(image, Queue[i], Queue[i + 1]);
	}
	if (sign == 2)
	{
		drawLine(image, startPt, endPt);
	}
	if (fin == true)
	{
		for (int i = 0; i < points.size(); i = i + 2)
		{
			drawLine(image, points[i], points[i + 1]);
		}
	}
	painter.drawImage(0, 0, image);
}
