#ifndef WIDGET_H
#define WIDGET_H

#include<QtWidgets>
#include <QWidget>

namespace Ui {
	class Widget;
}

class Widget : public QWidget
{
	Q_OBJECT

public:
	explicit Widget(QWidget *parent = nullptr);
	~Widget();

protected:
	virtual void paintEvent(QPaintEvent *event);
	virtual void mousePressEvent(QMouseEvent* event);
	virtual void mouseReleaseEvent(QMouseEvent *event);
	virtual void mouseMoveEvent(QMouseEvent *event);
private:
	QPoint startPt, endPt, Pt;
};

#endif // WIDGET_H