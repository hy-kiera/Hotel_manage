from django.db import models 
from django.utils import timezone
from django.contrib.auth.models import User

class Guest(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, primary_key='True', default="") 
    guest_pw = models.CharField(max_length=10)
#    first_name = models.CharField(max_length=10) 
#    last_name = models.CharField(max_length=10) 
    date_of_birth = models.DateField(default=timezone.now)  # 1998/01/01 
    sex = models.CharField(max_length=1)    # F 
    phone_num = models.CharField(max_length=13)     # 010-0000-0000 
#    e_mail = models.CharField(max_length=50) 
    language = models.CharField(max_length=20)    #Korea//1차 발표때 나온 지적에 따라 선호 언어로 변경 
    guest_class = models.CharField(max_length=10)   #silver 

    def __str__(self):
        return self.user