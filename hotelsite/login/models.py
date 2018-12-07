from django.db import models 
from django.utils import timezone

# class Guest(models.Model): 
#     guest_id = models.CharField(max_length=10) 
#     guest_pw = models.CharField(max_length=10)
#     first_name = models.CharField(max_length=10) 
#     last_name = models.CharField(max_length=10) 
#     date_of_birth = models.DateField(default=timezone.now)  # 1998/01/01 
#     sex = models.CharField(max_length=1)    # F 
#     phone_num = models.CharField(max_length=13)     # 010-0000-0000 
#     e_mail = models.CharField(max_length=50) 
#     language = models.CharField(max_length=20)    #Korea//1차 발표때 나온 지적에 따라 선호 언어로 변경 
#     guest_class = models.CharField(max_length=10)   #silver 

#     def __str__(self):
#         return self.guest_id

# class Staff(models.Model):
#     staff_id = models.IntegerField()
#     staff_pw = models.IntegerField()
#     first_name = models.CharField(max_length=10) 
#     last_name = models.CharField(max_length=10) 
#     work_start_time = models.CharField(max_length=20)   # 11/21 09:00 
#     work_end_time = models.CharField(max_length=20) # 11/21 18:00 
#     work_weekday = models.CharField(max_length=3)   # 월요일 
#     date_of_birth = models.CharField(max_length=10)  # 1998/01/01 
#     sex = models.CharField(max_length=1)    # F 
#     status = models.IntegerField()  # 0 : not working, 1 : working 
#     phone_num = models.CharField(max_length=13)     # 010-0000-0000 
#     # photo = models.FileField(upload_to=path) 
#     # dept = models.ForeignKey(Department, on_delete=models.CASCADE)  # it needs to check whethere it is foreign key  

#     def __str__(self):
#         return self.staff_id