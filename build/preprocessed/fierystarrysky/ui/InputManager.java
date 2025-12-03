package fierystarrysky.ui;

import javax.microedition.lcdui.*;
import fierystarrysky.Midlet;

/**
 * J2ME 输入框管理器，用于临时获取用户输入
 */
public abstract class InputManager extends BaseUIManager {

    protected final TextBox inputBox;

    public InputManager(String title, String initial, int maxLength, int constraints,
                        final Canvas canvas) {
        super(canvas);
        this.inputBox = new TextBox(title, initial, maxLength, constraints);

        final Command ok = new Command("确定", Command.OK, 1);
        final Command cancel = new Command("取消", Command.CANCEL, 2);
        inputBox.addCommand(ok);
        inputBox.addCommand(cancel);

        inputBox.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    onOk(inputBox.getString());
                } else if (c == cancel) {
                    onCancel();
                }
            }
        });
    }

    public void showInput() {
        show(inputBox);
    }

    protected abstract void onOk(String text);

    protected abstract void onCancel();
}