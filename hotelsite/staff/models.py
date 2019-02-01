from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
from login.models import Guest
from django.urls import reverse_lazy

def user_path(instance, filename): #파라미터 instance는 Photo 모델을 의미 filename은 업로드 된 파일의 파일 이름
    from random import choice
    import string # string.ascii_letters : ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz
    arr = [choice(string.ascii_letters) for _ in range(8)]
    pid = ''.join(arr) # 8자리 임의의 문자를 만들어 파일명으로 지정
    extension = filename.split('.')[-1] # 배열로 만들어 마지막 요소를 추출하여 파일확장자로 지정
    # file will be uploaded to MEDIA_ROOT/user_<id>/<random>
    return '%s/%s.%s' % (instance.owner.username, pid, extension) # 예 : wayhome/abcdefgs.png

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
    photo = models.ImageField(upload_to='images')
    dept = models.ForeignKey('Department', on_delete = models.CASCADE)

    def __str__(self):
        return self.user.username

    def get_absolute_url(self):
        url = reverse_lazy('detail', kwargs={'pk': self.pk})
        return url
      
class Department(models.Model):
    DEPT_IN_CHOICES=(
        ('BACK_OFFICE', 'back_office'),
        ('SALES_MARKETING', 'sales_marketing'),
        ('FRONT_OFFICE', 'front_office'),
        ('HOUSE_KEEPING', 'house_keeping'),
        ('FITNESS', 'fitness'),
        ('FOOD_BEVERAGE', 'food_beverage'),
        ('FINANCE', 'finance'),
    )
    name = models.CharField(max_length = 30, choices=DEPT_IN_CHOICES, unique=True) 
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
    guest = models.ForeignKey('login.Guest', on_delete=models.CASCADE)
    title = models.TextField() 
    text = models.TextField() 
    created_date = models.DateTimeField(default=timezone.now) 
    dept = models.ForeignKey('Department',on_delete=models.CASCADE, blank = True, null=True)
    room_num = models.ForeignKey('Room', on_delete=models.CASCADE, blank = True, null = True) 
    handle_or_not = models.DecimalField(decimal_places=0, max_digits=4, choices=HANDLE_IN_CHOICES, default = UNASSIGNED)
    staff = models.ForeignKey('staff',on_delete=models.CASCADE, blank = True, null=True)

    def __str__(self): 
        return self.title 

class Extracost(models.Model):
    PAYMENT_IN_CHOICES=(
        ('CREDIT CARD', 'credit card'),
        ('DEBIT CARD', 'debit card'),
        ('CHEQUE', 'cheque'),
        ('CASH', 'cash')
    )
    reserve_num = models.ForeignKey('Reservation', on_delete = models.CASCADE)
    total_cost = models.DecimalField(decimal_places=0, max_digits=10000000, default = 0)
    payment_type = models.CharField(max_length=20, choices=PAYMENT_IN_CHOICES, default='')

class Type(models.Model):
    TYPE_IN_CHOICES=(
        ('STANDARD SINGLE', 'Standard Single'),
        ('STANDARD DOUBLE', 'Standard Double'),
        ('SSTANDARD SUITE', 'Standard Suite')
    )
    room_type = models.CharField(max_length=20, choices=TYPE_IN_CHOICES, default='SINGLE', primary_key='True')
    capacity = models.DecimalField(decimal_places=0, max_digits=1, default = 0)
#    nights = models.DecimalField(decimal_places=0, max_digits=10, default=0)
    cost = models.DecimalField(decimal_places=0, max_digits=10000000, default=0)

class Reservation(models.Model):
    # created = models.DateTimeField(auto_now_add=True)
    reserve_num = models.DecimalField(decimal_places=0, max_digits=5, default=0, primary_key='True')
    room_num = models.ForeignKey('Room', on_delete=models.CASCADE, blank = True, null = True)
    guest_id = models.ForeignKey('login.Guest', on_delete = models.CASCADE)
    cost = models.CharField(max_length=10000000, default=0)
    date_start = models.DateTimeField(default=timezone.now)
    date_end = models.DateTimeField(default=timezone.now)
    book_time = models.DateTimeField(default=timezone.now)
    companion = models.DecimalField(decimal_places=0, max_digits=1, default=0)
    room_floor = models.DecimalField(decimal_places=0, max_digits=1, default='1')

    def __str__(self):
        return str(self.reserve_num)


class Membership(models.Model):
    CLASS_IN_CHOICES=(
        ('SILVER', 'silver'),
        ('GOLD', 'gold'),
        ('VIP', 'vip'),
        ('VVIP', 'vvip'),
    )
    guest_class = models.CharField(max_length=10, choices=CLASS_IN_CHOICES, primary_key='True')
    discount = models.CharField(max_length=10, default='0%')
    cumulative_cost = models.DecimalField(decimal_places=0, max_digits=100000000, default='0')
    reserve_count = models.DecimalField(decimal_places=0, max_digits=100, default='0')

class Guestcar(models.Model):
    guest_id = models.ForeignKey('login.Guest', on_delete = models.CASCADE)
    car_num = models.DecimalField(decimal_places=0, max_digits=10, default='0', primary_key='True')

class Charge(models.Model):
    room_num = models.ForeignKey('Room', on_delete=models.CASCADE, blank = True, null = True)
    staff_id = models.ForeignKey('Staff', on_delete = models.CASCADE)
