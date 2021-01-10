/*
 *date:2018_10_19
 */

#include "widget.h"
#include <QVector>
#include <iostream>
#include <math.h>

using namespace std;
#define W 1920
#define H 1040
#define N 3000 //曲线点数

enum color
{
	pink,
	blue,
	red,
	yellow
};

QVector<QPoint> point;
QVector<QPoint> bezier_point;
int point_count = 0;
int con = 0;             //用于防止拖动端点不跟手
int now = 0;             //记录活动的端点
bool right_move = false; //解决新增端点时卡顿

Widget::Widget(QWidget *parent) : QWidget(parent)
{
}
Widget::~Widget()
{
}

typedef struct
{
	double X;
	double Y;
} PointF;

PointF tmp_points[100];

PointF bezier_interpolation_func(double t, PointF *points)
{
	//PointF *tmp_points = (PointF *)malloc(point_count * sizeof(PointF));
	//内存溢出？
	for (int i = 1; i < point_count; ++i)
	{
		for (int j = 0; j < point_count - i; ++j)
		{
			if (i == 1)
			{
				tmp_points[j].X = (double)(points[j].X * (1 - t) + points[j + 1].X * t);
				tmp_points[j].Y = (double)(points[j].Y * (1 - t) + points[j + 1].Y * t);
				continue;
			}
			tmp_points[j].X = (double)(tmp_points[j].X * (1 - t) + tmp_points[j + 1].X * t);
			tmp_points[j].Y = (double)(tmp_points[j].Y * (1 - t) + tmp_points[j + 1].Y * t);
		}
	}
	return tmp_points[0];
}

void draw_bezier_curves(PointF *points, PointF *out_points)
{
	double step = 1.0 / N;
	double t = 0;
	for (int i = 0; i < N; i++)
	{
		PointF temp_point = bezier_interpolation_func(t, points); // 计算插值点
		t += step;
		out_points[i] = temp_point;
	}
}

void bezier(QVector<QPoint> point)
{
	QPoint bpoint;
	PointF pointf; // 输入点
	int x, y;
	PointF *in = (PointF *)malloc(point_count * sizeof(PointF));
	for (int i = 0; i < point_count; i++)
	{
		in[i].X = point[i].x();
		in[i].Y = point[i].y();
	}
	//PointF *out = (PointF *)malloc(num_ * sizeof(PointF));// 输出点数组
	PointF out[N];
	draw_bezier_curves(in, out); // 二阶贝塞尔曲线

	for (int j = 0; j < N; j++) // 输出路径点
	{
		bpoint.setX(out[j].X);
		bpoint.setY(out[j].Y);
		bezier_point.push_back(bpoint);
	}
	free(in);
}

void Widget::mouseMoveEvent(QMouseEvent *event)
{
	QPoint p1;
	int x, y;
	int i;
	bool if_in;
	p1 = event->pos();
	x = p1.x();
	y = p1.y();
	if (right_move == true)
	{
		now = point_count - 1;
		point[now].setX(x);
		point[now].setY(y);
		if (point_count > 2)
		{
			bezier_point.clear();
			bezier(point);
		}
		update();
	}
	else
	{
		if (now == 0)
		{
			for (i = 0; i < point_count; i++)
			{
				if (x > point[i].x() - 10 && x < point[i].x() + 10 && y > point[i].y() - 10 && y < point[i].y() + 10)
				{
					now = i;
					break;
				}
			}
		}
		if (con > 0 || x > point[now].x() - 10 && x < point[now].x() + 10 && y > point[now].y() - 10 && y < point[now].y() + 10)
		{
			if_in = true;
		}
		else
		{
			if_in = false;
		}
		if (if_in == true)
		{
			con = 5;
			point[now].setX(x);
			point[now].setY(y);
		}
		if (point_count > 2)
		{
			bezier_point.clear();
			bezier(point);
		}
		update();
	}
}
void Widget::mouseReleaseEvent(QMouseEvent *event)
{
	con = 0;
	now = 0;
	right_move = false;
}

void Widget::mousePressEvent(QMouseEvent *event)
{
	if (event->button() == Qt::RightButton)
	{
		right_move = true;
		Pt = event->pos();
		point.push_back(Pt);
		point_count++;
		if (point_count > 2)
		{
			bezier_point.clear();
			bezier(point);
		}
		update();
	}
	else if (event->button() == Qt::LeftButton)
	{
		update();
	}
	else if (event->button() == Qt::MidButton)
	{
		if (point_count > 0)
		{
			--point_count;
			point.pop_back();
			bezier_point.clear();
			bezier(point);
			update();
		}
	}
}

void qpoint(QImage &image, int x, int y, color col)
{
	unsigned char *ptrRow = image.scanLine(y);
	if (col == blue)
	{
		ptrRow[x * 3 + 0] = 78;  //red
		ptrRow[x * 3 + 1] = 201; //green
		ptrRow[x * 3 + 2] = 176; //blue
	}
	else if (col == pink)
	{
		ptrRow[x * 3 + 0] = 205; //red 205 145 157
		ptrRow[x * 3 + 1] = 145; //green 216 145 159
		ptrRow[x * 3 + 2] = 157; //blue
	}
	else if (col == red)
	{
		ptrRow[x * 3 + 0] = 255; //red
		ptrRow[x * 3 + 1] = 30;  //green
		ptrRow[x * 3 + 2] = 30;  //blue
	}
	else if (col == yellow)
	{
		ptrRow[x * 3 + 0] = 255; //red
		ptrRow[x * 3 + 1] = 240; //green
		ptrRow[x * 3 + 2] = 80;  //blue
	}
}

void qpoint_color(QImage &image, QPoint p, color col)
{
	if (p.x() > 1 && p.x() < W - 1 && p.y() > 1 && p.y() < H - 1)
	{
		int x = p.x();
		int y = p.y();
		qpoint(image, x, y, col);
	}
}

void drawLine(QImage &image, QPoint pt1, QPoint pt2, color col)
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
	if (a >= 0 && b <= 0 && a >= -b || a <= 0 && b >= 0 && -a >= b)
	{
		a = p2x - p1x;
		b = p1y - p2y;
		d = b - 0.5 * a;
		d1 = b - a;
		flag = 11;
	}
	else if (a > 0 && b > 0 && a >= b || a < 0 && b < 0 && -a > -b)
	{
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
				qpoint(image, x, y, col);

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
				qpoint(image, x, y, col);
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
				qpoint(image, x, y, col);
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
				qpoint(image, x, y, col);
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
				qpoint(image, x, y, col);
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
				qpoint(image, x, y, col);
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
				qpoint(image, x, y, col);
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
				qpoint(image, x, y, col);
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

void print_cross(QImage &image, QPoint pt)
{
	QPoint ptup = pt, ptdown = pt, ptleft = pt, ptright = pt;
	ptup.setY(pt.y() + 5);
	ptdown.setY(pt.y() - 5);
	ptleft.setX(pt.x() - 5);
	ptright.setX(pt.x() + 5);
	drawLine(image, ptleft, ptright, yellow);
	drawLine(image, ptup, ptdown, yellow);
}

void all_cross(QImage &image, QVector<QPoint> pt)
{
	for (int i = 0; i < point_count; i++)
	{
		print_cross(image, point[i]);
	}
}

QImage image = QImage(W, H, QImage::Format_RGB888);

void Widget::paintEvent(QPaintEvent *event)
{
	QPainter painter(this);
	image.fill(QColor(1, 1, 1)); //backgrond color
	for (int i = 0; i < point_count - 1; i++)
	{
		drawLine(image, point[i], point[i + 1], pink);
	}
	if (point_count > 2)
	{
		for (int i = 0; i < N; ++i)
		{
			qpoint_color(image, bezier_point[i], blue);
		}
	}
	all_cross(image, point);
	painter.drawImage(0, 0, image);
}