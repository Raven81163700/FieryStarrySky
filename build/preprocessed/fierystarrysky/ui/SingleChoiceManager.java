package fierystarrysky.ui;

import javax.microedition.lcdui.*;
import fierystarrysky.Midlet;

/**
 * J2ME 单选菜单管理器
 */
public abstract class SingleChoiceManager extends BaseUIManager {

    protected final List list;

    public SingleChoiceManager(String title, final String[] options,
            int initialIndex, final Canvas canvas) {
        super(canvas);
        this.list = new List(title, Choice.EXCLUSIVE, options, null);

        // 默认选中
        if (initialIndex >= 0 && initialIndex < options.length) {
            list.setSelectedIndex(initialIndex, true);
        }

        // 添加命令
        final Command ok = new Command("确定", Command.OK, 1);
        final Command cancel = new Command("取消", Command.CANCEL, 2);
        list.addCommand(ok);
        list.addCommand(cancel);

        list.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    onOk(list.getSelectedIndex());
                } else if (c == cancel) {
                    onCancel();
                }
            }
        });
    }

    public void showMenu() {
        show(list);
    }

    protected abstract void onOk(int selectedIndex);

    protected abstract void onCancel();
}