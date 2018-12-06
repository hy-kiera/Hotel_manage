from django import forms
from django.contrib.auth.models import User
from .models import Guest

class LoginForm(forms.ModelForm):
    class Meta:
        model = User
        fields = ['username', 'password']


class UserForm(forms.ModelForm):
    class Meta:
        model = User
        fields = [
            'username',
            'password',
            ]
        #widgets={
        #    'username': forms.TextInput(attrs={'class':'form-control'}),
        #    'email': forms.EmailInput(attrs={'class':'form-control'}),
        #    'password': forms.PasswordInput(attrs={'class':'form-control'}),
        #    'first_name': forms.TextInput(attrs={'class':'form-control'}), 
        #    'last_name': forms.TextInput(attrs={'class':'form-control'}), 

        #}
        labels ={
            'username':'아이디',
            'password':'패스워드',
        }


class GuestForm(forms.ModelForm):
    class Meta:
        model = Guest
        fields = [
            'guest_id', 
            'first_name', 
            'last_name', 
            'date_of_birth',
            'sex',
            'phone_num',
            'e_mail',
            'language',

        ]

        #widgets = {
        #    'user': forms.TextInput(attrs={'class':'form-control'}), 
            #'first_name': forms.TextInput(attrs={'class':'form-control'}), 
            #'last_name': forms.TextInput(attrs={'class':'form-control'}), 
        #    'date_of_birth': forms.DateInput(attrs={'class':'form-control'}),
        #    'sex': forms.TextInput(attrs={'class':'form-control', 'placeholder':'F/M'}),
        #    'phone_num': forms.TextInput(attrs={'class':'form-control', 'placeholder':'010-000-0000'}),
            #'e_mail': forms.EmailInput(attrs={'class':'form-control'}),
        #    'language': forms.TextInput(attrs={'class':'form-control'}),
        #}
        

        labels ={
            'guest_id':'아이디', 
            'first_name':'이름', 
            'last_name':'성', 
            'date_of_birth':'생년월일',
            'sex':'성별',
            'phone_num':'휴대전화번호',
            'e_mail':'e_mail',
            'language':'선호 언어',
        }