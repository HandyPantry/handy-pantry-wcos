/* eslint-disable @typescript-eslint/naming-convention */
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Product } from '../product';
import { ProductService } from '../product.service';

@Component({
  selector: 'app-add-product',
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.scss']
})
export class AddProductComponent implements OnInit {

  addProductForm: FormGroup;

  product: Product;

  addProductValidationMessages;

  constructor(private fb: FormBuilder, private productService: ProductService,
     private snackBar: MatSnackBar, private router: Router) {
  }

  public static createValidationForm() {
    return {
      product_name: [
        {type: 'required', message: 'Product\'s name is required'},
        {type: 'minlength', message: 'Product name must be at least 1 character'},
        {type: 'maxlength', message: 'Product name must be at less than 100 characters'}
      ],
      description: [
        {type: 'minlength', message: 'Product description must be at least 1 character'},
        {type: 'maxlength', message: 'Product description must be at less than 200 characters'}
      ],
      brand: [
        {type: 'minlength', message: 'Product brand must be at least 1 character'},
        {type: 'maxlength', message: 'Product brand must be at less than 100 characters'}
      ],
      category: [
        {type: 'required', message: 'Product category is required'},
        {type: 'pattern', message: 'Category must be, baked goods, produce, meat, dairy, frozen foods, baking supplies,'
        + 'beverages, cleaning products, miscellaneous, deli, herbs and spices, paper products, pet supplies, staples, toiletries'},
      ],
      store: [
        {type: 'required', message: 'Product store is required'},
        {type: 'minlength', message: 'Product store must be at least 1 character'},
        {type: 'maxlength', message: 'Product store must be at less than 100 characters'}
      ],
      location: [
        {type: 'minlength', message: 'Product location must be at least 1 character'},
        {type: 'maxlength', message: 'Product location must be at less than 100 characters'}
      ],
      notes: [
        {type: 'minlength', message: 'Product notes must be at least 1 character'},
        {type: 'maxlength', message: 'Product notes must be at less than 200 characters'}
      ],
      lifespan: [
        {type: 'min', message: 'Product lifespan must be at least 1'},
        {type: 'max', message: 'Product lifespan must be at less than 1000000'},
        {type: 'pattern', message: 'Lifespan must be a whole number'}
      ],
      threshold: [
        {type: 'required', message: 'Product\'s threshold is required'},
        {type: 'min', message: 'Product threshold must be at least 1'},
        {type: 'max', message: 'Product threshold must be at less than 1000000'},
        {type: 'pattern', message: 'Threshold must be a whole number'}
      ]
    };
  }

  createForms() {
    this.addProductForm = this.fb.group({
      product_name: new FormControl('', Validators.compose([
        Validators.required, Validators.minLength(1), Validators.maxLength(100),
      ])),
      description: new FormControl('', Validators.compose([
        Validators.minLength(1), Validators.maxLength(200),
      ])),
      brand: new FormControl('', Validators.compose([
        Validators.minLength(1), Validators.maxLength(100),
      ])),
      category: new FormControl('', Validators.compose([
        Validators.required,
        Validators.pattern('^(baked goods|baking supplies|beverages|cleaning products|dairy|deli|' +
          'frozen foods|herbs and spices|meat|paper products|pet supplies|produce|staples|toiletries|miscellaneous)$')
      ])),
      store: new FormControl('', Validators.compose([
        Validators.required, Validators.pattern('^(Willies|Pomme de Terre|Pomme de Terre/Willies|Real Food Hub|Other)$')
      ])),
      location: new FormControl('', Validators.compose([
        Validators.minLength(1), Validators.maxLength(100),
      ])),
      notes: new FormControl('', Validators.compose([
        Validators.minLength(1), Validators.maxLength(200),
      ])),
      lifespan: new FormControl('', Validators.compose([
        Validators.min(1), Validators.max(1000000), Validators.pattern('^[0-9]+$')
      ])),
      threshold: new FormControl('', Validators.compose([
        Validators.required, Validators.min(1), Validators.max(1000000), Validators.pattern('^[0-9]+$')
      ])),
    });
  }

  ngOnInit(): void {
    this.addProductValidationMessages = AddProductComponent.createValidationForm();
    this.createForms();
  }

  /* istanbul ignore next */
  submitForm() {
    console.log(this.addProductForm.value);
    this.productService.addProduct(this.addProductForm.value).subscribe(newID => {
      this.snackBar.open('Added Product' + this.addProductForm.value.product_name, null, {
        duration: 2000,
      });
      this.router.navigate(['/products/', newID]);
    }, err => {
      this.snackBar.open('Failed to add the product', 'OK', {
        duration: 5000,
      });
    });
  }


}
