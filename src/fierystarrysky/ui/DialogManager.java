package fierystarrysky.ui;

import javax.microedition.lcdui.*;

/**
 * 对话框管理器（基于 Alert）
 */
public abstract class DialogManager extends BaseUIManager {

    protected final Alert alert;

    public DialogManager(String title, String message, int type, final Canvas canvas) {
        super(canvas);

        // type 对应 AlertType，例如 AlertType.INFO, AlertType.ERROR 等
        this.alert = new Alert(title, message, null, getAlertType(type));
        alert.setTimeout(Alert.FOREVER);

        // 添加命令
        final Command ok = new Command("确定", Command.OK, 1);
        final Command cancel = new Command("取消", Command.CANCEL, 2);
        alert.addCommand(ok);
        alert.addCommand(cancel);

        alert.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    onOk();
                } else if (c == cancel) {
                    onCancel();
                }
            }
        });
    }

    public void showDialog() {
        show(alert);
    }

    protected abstract void onOk();

    protected abstract void onCancel();

    private static AlertType getAlertType(int type) {
        switch (type) {
            case 1:
                return AlertType.INFO;
            case 2:
                return AlertType.WARNING;
            case 3:
                return AlertType.ERROR;
            case 4:
                return AlertType.ALARM;
            default:
                return AlertType.CONFIRMATION;
        }
    }
}