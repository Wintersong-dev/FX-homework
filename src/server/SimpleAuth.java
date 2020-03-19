package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuth implements AuthService {

    private class UserData {
        String login, pwd, nickname;
        UserData(String _login, String _pwd, String _nickname) {
            login = _login;
            pwd = _pwd;
            nickname = _nickname;
        }
    }

    List<UserData> users;

    SimpleAuth() {
        users = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            users.add(new UserData("login" + i, "password" + i, "nickname" + i));
        }

    }

    @Override
    public String getNickname(String login, String pwd) {
        for(UserData user : users) {
            if (user.login.equals(login) && user.pwd.equals(pwd)) {
                return user.nickname;
            }
        }
        return null;
    }
}
