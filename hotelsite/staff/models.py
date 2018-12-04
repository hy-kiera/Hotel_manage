from django.db import models
from django.contrib.auth.models import User

class Room(models.Model): 
 
    TYPE_IN_CHOICES=(
        ('SINGLE', 'Single'),
        ('DOUBLE', 'Double'),
        ('SUITE', 'Suite')
    )
    
    STATUS_CLEAN_IN_CHOICES=(
        ('CLEAN', 'Clean'),
        ('UNCLEAN', 'Unclean')
    )
    room_num = models.DecimalField(decimal_places=0, max_digits=4, primary_key='True') 
    room_type = models.CharField(max_length=10, choices=TYPE_IN_CHOICES, default='SINGLE') #Suite 
    status_clean = models.CharField(max_length=10, choices=STATUS_CLEAN_IN_CHOICES, default='CLEAN')  # 0 : 사용중, 1 : 사용가능, 3 : 청소 필요, 4 : 예약
    
class Staff(models.Model):

    DEPT_IN_CHOICES=(
        ('BACK_OFFICE', 'back_office'),
        ('SALES_MARKETING', 'sales_marketing'),
        ('FRONT_OFFICE', 'front_office'),
        ('HOUSE_KEEPING', 'house_keeping'),
        ('FITNESS', 'fitness'),
        ('FOOD_BERVERAGE', 'food_berverage'),
        ('FINANCE', 'finance'),
    )

    SEX=(
        ('MALE', 'male'),
        ('FEMALE', 'female')
    )

    STATUS=(
        ('WORKING', 'working'),
        ('RESTING', 'resting'),
        ('HOLIDAY', 'holiday'),
        ('ABSENCE', 'absence')
    )

    user = models.OneToOneField(User, on_delete=models.CASCADE, primary_key='True', default="")
    # staff_id = models.CharField(max_length=10, primary_key='True')
    # name_first = models.CharField(max_length=2)
    # name_last = models.CharField(max_length=3)
    work_start_time = models.DateTimeField(auto_now='True') # 갱신
    work_end_time = models.DateTimeField(auto_now='True')
    work_weekday = models.CharField(max_length=3)    # 월요일
    date_of_birth = models.CharField(max_length=10)  # 0000.00.00
    sex = models.CharField(max_length=6, choices=SEX)
    status = models.CharField(max_length=7, choices=STATUS)
    phone_num = models.CharField(max_length=13) # 010-0000-0000
    # photo = models.ImageField()
    dept = models.CharField(max_length=14, choices=DEPT_IN_CHOICES)