package com.cqm.realmdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.result)
    TextView result;
    @BindView(R.id.insert)
    Button insert;
    @BindView(R.id.delete)
    Button delete;
    @BindView(R.id.update)
    Button update;
    @BindView(R.id.query)
    Button query;


    private RealmAsyncTask addTask;
    private RealmResults<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    //异步添加数据
    private void asyncAddUser(final User user) {
        Realm realm = Realm.getDefaultInstance();
        addTask = realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(user);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_LONG).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_LONG).show();
            }
        });

    }


    @OnClick({R.id.insert, R.id.delete, R.id.update, R.id.query})
    public void onClick(View view) {
        Realm realm = Realm.getDefaultInstance();
        switch (view.getId()) {
            //添加数据
            case R.id.insert: {
                realm.beginTransaction();
                User user = realm.createObject(User.class);
                user.setAge("2");
                user.setName("jhon");
                String id = String.valueOf(System.currentTimeMillis());
                user.setId(id);
                realm.commitTransaction();


                User user1 = new User();
                user1.setAge("5");
                user1.setName("jack");
                String id1 = String.valueOf(System.currentTimeMillis());
                user1.setId(id1);
                asyncAddUser(user1);
            }
            break;
            //删除数据
            case R.id.delete: {
                RealmResults<User> datas = realm.where(User.class).findAll();
                final RealmResults<User> users = datas.sort("id");
                if (users != null && users.size() > 0) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            boolean flag = users.deleteFirstFromRealm();
                            if (flag) {
                                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "无数据，无法删除", Toast.LENGTH_LONG).show();
                }


            }
            break;
            //修改数据
            case R.id.update: {
                User user = realm.where(User.class).equalTo("name", "jhon").findFirst();
                if (user != null) {
                    realm.beginTransaction();
                    user.setName("ming");
                    user.setAge("10");
                    realm.commitTransaction();

                    Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(MainActivity.this, "数据不存在，无法修改", Toast.LENGTH_LONG).show();
                }


            }
            break;
            //查询数据
            case R.id.query: {
                String str = "";
                RealmResults<User> users = realm.where(User.class).findAll();
                users = users.sort("id");
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    str += user.toString() + "\n";
                }
                if (TextUtils.isEmpty(str)) {
                    result.setText("无数据");
                } else {
                    result.setText(str);
                }

//                final Realm mRealm = Realm.getDefaultInstance();
//                mUsers= mRealm.where(User.class).findAllAsync();
//                mUsers.addChangeListener(new RealmChangeListener<RealmResults<User>>() {
//                    @Override
//                    public void onChange(RealmResults<User> element) {
//                        List<User> datas = mRealm.copyFromRealm(element);
//                        String text = "";
//                        for (int i = 0; i < datas.size(); i++) {
//                            User user = datas.get(i);
//                            text += user.toString() + "\n";
//                        }
//                        Toast.makeText(MainActivity.this,text,Toast.LENGTH_LONG).show();
//                    }
//                });
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addTask != null && !addTask.isCancelled()) {
            addTask.cancel();
        }

        if (mUsers != null) {
            mUsers.removeChangeListeners();
        }
    }
}
