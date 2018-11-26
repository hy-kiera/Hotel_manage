# Generated by Django 2.1.3 on 2018-11-25 03:45

from django.db import migrations, models
import django.utils.timezone


class Migration(migrations.Migration):

    dependencies = [
        ('login', '0001_initial'),
    ]

    operations = [
        migrations.RenameField(
            model_name='guest',
            old_name='nation',
            new_name='language',
        ),
        migrations.AlterField(
            model_name='guest',
            name='date_of_birth',
            field=models.DateField(default=django.utils.timezone.now),
        ),
    ]