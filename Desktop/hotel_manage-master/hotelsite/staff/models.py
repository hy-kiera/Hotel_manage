from django.db import models

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
    type = models.CharField(max_length=10, choices=TYPE_IN_CHOICES, default = 'SINGLE') #Suite 
    status_clean = models.CharField(max_length=10, choices=STATUS_CLEAN_IN_CHOICES, default='CLEAN')  # 0 : 사용중, 1 : 사용가능, 3 : 청소 필요, 4 : 예약
    
