package com.example.imearthz.testapp2;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.imearthz.testapp2.module.Account;

public class RegisterFragment extends Fragment {

    private static final String TAG = "REGISTER";

    private SQLiteDatabase myDB;
    private Account account;
    private ProgressDialog zLoading;

    private EditText zUsername;
    private EditText zPassword;
    private EditText zRepassword;
    private EditText zFullname;
    private EditText zPhonenumber;
    private Button zSignup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myDB = getActivity().openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null);
        account = Account.getAccountInstance();
        zLoading = new ProgressDialog(getActivity());
        registerFragmentElements();
        initSignup();
    }

    private void registerFragmentElements() {
        Log.d(TAG, "Get Elements");
        zUsername = getView().findViewById(R.id.register_username);
        zPassword = getView().findViewById(R.id.register_password);
        zRepassword = getView().findViewById(R.id.register_repassword);
        zFullname = getView().findViewById(R.id.register_fullname);
        zPhonenumber = getView().findViewById(R.id.register_phonenumber);
        zSignup = getView().findViewById(R.id.register_signupbtn);
    }

    private void initSignup() {
        zSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Signup : clicked");
                zLoading.setMessage("กำลังสมัครสมาชิก...");
                zLoading.setCanceledOnTouchOutside(false);
                zLoading.setCancelable(false);
                zLoading.show();

                String username = zUsername.getText().toString();
                String password = zPassword.getText().toString();
                String repassword = zRepassword.getText().toString();
                String fullname = zFullname.getText().toString();
                String phonenumber = zPhonenumber.getText().toString();

                if (username.isEmpty() || password.isEmpty() || repassword.isEmpty() || fullname.isEmpty() || phonenumber.isEmpty()) {
                    Log.d(TAG, "field is empty");
                    Toast.makeText(getActivity(), "กรุณาใส่ข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show();
                    zLoading.dismiss();
                } else if (password.length() < 6 || repassword.length() < 6) {
                    Log.d(TAG, "password more than 6");
                    Toast.makeText(getActivity(), "รหัสผ่านต้องมากกว่า 6 ตัวอักษร", Toast.LENGTH_SHORT).show();
                    zLoading.dismiss();
                } else if (!password.equals(repassword) || !repassword.equals(password)) {
                    Log.d(TAG, "Both passwords must be the same");
                    Toast.makeText(getActivity(), "กรุณาใส่รหัสผ่านทั้ง 2 ช่องให้เหมือนกัน", Toast.LENGTH_SHORT).show();
                    zLoading.dismiss();
                } else if(phonenumber.length() != 10) {
                    Log.d(TAG, "The phone number is not complete");
                    Toast.makeText(getActivity(), "กรุณาใส่เบอร์โทรศัพท์ให้ครบ", Toast.LENGTH_SHORT).show();
                    zLoading.dismiss();
                } else {
                    Cursor cursor = myDB.rawQuery("select * from account where username = '" + username + "'", null);
                    if (cursor.getCount() != 1) {

                        ContentValues registerAccount = new ContentValues();
                        registerAccount.put("username", username);
                        registerAccount.put("password", password);

                        Log.d(TAG, "Insert new account");
                        myDB.insert("account", null, registerAccount);
                        account.setUsername(username);
                        account.setPassword(password);

                        Cursor cursorPrimaryid = myDB.rawQuery("select * from account where username = '" + username + "'", null);
                        while (cursorPrimaryid.moveToNext()) {
                            ContentValues registerMy = new ContentValues();
                            registerMy.put("fullname", fullname);
                            registerMy.put("phonenumber", phonenumber);
                            registerMy.put("account_id", cursorPrimaryid.getInt(0));
                            myDB.insert("my", null, registerMy);
                            account.setPrimaryid(cursorPrimaryid.getInt(0));
                            account.setFullname(fullname);
                            account.setPhonenumber(phonenumber);
                        }
                        cursorPrimaryid.close();
                        cursor.close();
                        Toast.makeText(getActivity(), "สมัครสมาชิกเรียบร้อย", Toast.LENGTH_LONG).show();
                        zLoading.dismiss();

                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_view, new LoginFragment()).commit();
                    } else {
                        Toast.makeText(getActivity(), "username นี้มีผู้ใช้อื่นใช้แล้ว", Toast.LENGTH_LONG).show();
                        cursor.close();
                        zLoading.dismiss();
                    }
                }
            }
        });
    }
}