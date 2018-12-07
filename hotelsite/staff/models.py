from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone


class Room(models.Model):
    STATUS_CLEAN_IN_CHOICES=(
        ('CLEAN', 'Clean'),
        ('UNCLEAN', 'Unclean')
    )
    room_num = models.DecimalField(decimal_places=0, max_digits=4, primary_key='True', default = 0) 
    room_type = models.ForeignKey('Type', on_delete = models.CASCADE) #Suite
    status_clean = models.CharField(max_length=10, choices=STATUS_CLEAN_IN_CHOICES, default='CLEAN')  # 0 : 사용중, 1 : 사용가능, 3 : 청소 필요, 4 : 예약
    room_floor = models.DecimalField(decimal_places=0, max_digits=1, default='1')

    def __str__(self):
        return str(self.room_num)+'호'

class Type(models.Model):
    TYPE_IN_CHOICES=(
        ('STANDARD SINGLE', 'Standard Single'),
        ('STANDARD DOUBLE', 'Standard Double'),
        ('SSTANDARD SUITE', 'Standard Suite')
    )
    room_type = models.CharField(max_length=20, choices=TYPE_IN_CHOICES, default='SINGLE', primary_key='True')
#    capacity = models.DecimalField(decimal_places=0, max_digits=1, default = 0)
    nights = models.DecimalField(decimal_places=0, max_digits=10, default=0)
    cost = models.DecimalField(decimal_places=0, max_digits=10000000, default=0)

class Reservation(models.Model):
    # created = models.DateTimeField(auto_now_add=True)
    reserve_num = models.DecimalField(decimal_places=0, max_digits=5, default=0, primary_key='True')
    room_num = models.ForeignKey('Room', on_delete=models.CASCADE, blank = True, null = True)
    guest_id = models.CharField(max_length=10, default='')
    cost = models.CharField(max_length=10000000, default=0)
    date_start = models.CharField(max_length=20, default='0000-00-00')
    date_end = models.CharField(max_length=20, default='0000-00-00')
    book_time = models.DateTimeField(default=timezone.now)
    companion = models.DecimalField(decimal_places=0, max_digits=1, default=0)
    room_floor = models.DecimalField(decimal_places=0, max_digits=1, default='1')

        
class Staff(models.Model):
    
    SEX_IN_CHOICES=(
        ('MALE', 'male'),
        ('FEMALE', 'female')
    )
        
    STATUS_IN_CHOICES=(
        ('WORKING', 'working'),
        ('RESTING', 'resting'),
        ('HOLIDAY', 'holiday'),
        ('ABSENCE', 'absence'),
    )
                    
    user = models.OneToOneField(User, on_delete=models.CASCADE, default="")
    staff_id = models.DecimalField(decimal_places=0,primary_key='True', max_digits=10, default=0)
    name_first = models.CharField(max_length=2, default="")
    name_last = models.CharField(max_length=3, default="")
    work_start_time = models.CharField(max_length=20, default='0000-00-00') # 갱신
    work_end_time = models.CharField(max_length=20, default='0000-00-00')
    work_weekday = models.CharField(max_length=3)    # 월요일
    date_of_birth = models.CharField(max_length=10)  # 0000.00.00
    sex = models.CharField(max_length=6, choices=SEX_IN_CHOICES)
    status = models.CharField(max_length=7, choices=STATUS_IN_CHOICES)
    phone_num = models.CharField(max_length=13) # 010-0000-0000
     # photo = models.ImageField()
    dept = models.ForeignKey('Department', on_delete = models.CASCADE, default="")

    
class Department(models.Model):
  
    DEPT_IN_CHOICES=(
        ('BACK_OFFICE', 'back_office'),
        ('SALES_MARKETING', 'sales_marketing'),
        ('FRONT_OFFICE', 'front_office'),
        ('HOUSE_KEEPING', 'house_keeping'),
        ('FITNESS', 'fitness'),
        ('FOOD_BERVERAGE', 'food_berverage'),
        ('FINANCE', 'finance'),
    )
    
    name = models.CharField(max_length = 30, choices=DEPT_IN_CHOICES)
    role = models.TextField() 
    tel_num = models.CharField(max_length = 20) 

    def __str__(self): 
        return self.name 



class Request_post(models.Model): 
    #처리여부 옵션
    UNASSIGNED = 1
    NOT_YET=2
    DONE=3

    HANDLE_IN_CHOICES=(
        (UNASSIGNED, '미배정'),
        (NOT_YET, '처리중'),
        (DONE, '처리완료'),
    )

    #요청번호는 자체 제공하는 pk이용 
    author =  models.ForeignKey('auth.User', on_delete=models.CASCADE) 
    title = models.TextField() 
    text = models.TextField() 
    created_date = models.DateTimeField(default=timezone.now) 
    dept = models.ForeignKey('Department',on_delete=models.CASCADE, blank = True, null=True)
    room_num = models.ForeignKey('Room', on_delete=models.CASCADE, blank = True, null = True) 
    handle_or_not = models.DecimalField(decimal_places=0, max_digits=4, choices=HANDLE_IN_CHOICES, default = UNASSIGNED)

    def __str__(self): 
        return self.title 
