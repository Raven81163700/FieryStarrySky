package fierystarrysky.ui;

import javax.microedition.lcdui.*;
import fierystarrysky.Midlet;

/**
 * J2ME 图文信息展示管理器
 */
public abstract class FormManager extends BaseUIManager {

    protected final Form form;

    public FormManager(String title, final Canvas canvas) {
        super(canvas);
        this.form = new Form(title);

        // 添加命令
        final Command ok = new Command("确定", Command.OK, 1);
        final Command cancel = new Command("取消", Command.CANCEL, 2);
        form.addCommand(ok);
        form.addCommand(cancel);

        form.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    onOk();
                } else if (c == cancel) {
                    onCancel();
                }
            }
        });
    }

    /**
     * 添加一段文字
     */
    public void addText(String text) {
        form.append(new StringItem(null, text));
    }

    /**
     * 添加一张图片
     */
    public void addImage(Image img) {
        form.append(new ImageItem(null, img, ImageItem.LAYOUT_DEFAULT, null));
    }

    public void showForm() {
        show(form);
    }

    protected abstract void onOk();

    protected abstract void onCancel();
}
