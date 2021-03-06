import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Router} from "@angular/router";
import {CookieService} from "ngx-cookie-service";
import {Observable} from "rxjs/Observable";
import {MatSnackBar} from "@angular/material";
import {Doctor} from "../domain/doctor";

@Injectable()
export class AuthenticationService {

    private tokenRequest = 'api/uaa/oauth/token';
    private currentAccount = 'api/doctors/current';

    constructor(private router: Router, private http: HttpClient, private cookieService: CookieService, private snackBar: MatSnackBar) {
    }

    public obtainAccessToken(loginData) {
        let data = "scope=ui&grant_type=password&username=" + loginData.username + "&password=" + loginData.password;

        let headers = new HttpHeaders({
            'Content-type': 'application/x-www-form-urlencoded; charset=utf-8',
            'Authorization': 'Basic YnJvd3Nlcjo='
        });
        let options = {
            headers: headers
        };

        this.http.post(this.tokenRequest, data, options)
            .subscribe(
                data => this.saveCredentials(data, loginData.username),
                err => this.displayMessage('Invalid Credentials'));
    }

    public getCurrentAccount(): Observable<Doctor> {
        let token = this.getOauthToken();

        let headers = new HttpHeaders({'Authorization': 'Bearer ' + token});
        let options = {
            headers: headers
        };

        return this.http.get<Doctor>(this.currentAccount, options);
    }

    public getOauthToken(): string {
        return this.cookieService.get('access_token');
    }

    public getUsername(): string {
        return this.cookieService.get("username");
    }

    public checkCredentials() {
        if (!this.cookieService.check('access_token')) {
            this.router.navigate(['/login']);
        }
    }

    public isUserLogin(): boolean {
        return this.cookieService.check('access_token');
    }

    public logout() {
        this.cookieService.delete('access_token');
        this.cookieService.delete('username');
        this.router.navigate(['/login']);
    }

    private saveCredentials(token, username) {
        let expireDate = new Date().getTime() + (1000 * token.expires_in);

        this.cookieService.set("access_token", token.access_token, expireDate);
        this.cookieService.set("username", username, expireDate);

        this.router.navigate(['/']);
        window.location.reload();
    }

    private displayMessage(message) {
        this.snackBar.open(message, "", {
            duration: 5000,
        });
    }

}