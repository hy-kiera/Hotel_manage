from django.db import models
from django.utils import timezone

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
    room_num = models.DecimalField(decimal_places=0, max_digits=4, primary_key = 'True') 
    room_type = models.CharField(max_length=10, choices=TYPE_IN_CHOICES, default = 'SINGLE') #Suite 
    status_clean = models.CharField(max_length=10, choices=STATUS_CLEAN_IN_CHOICES, default='CLEAN')  # 0 : 사용중, 1 : 사용가능, 3 : 청소 필요, 4 : 예약

    def __str__(self):
        return str(self.room_num)+'호'

class Department(models.Model): 
    name = models.CharField(max_length = 30) 
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

