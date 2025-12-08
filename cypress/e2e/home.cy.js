// E2E tests for the Nordic Electronics SPA using Cypress

  const URL = 'http://localhost:3000';

  const navTitles = ["Home", "Brand", "Categories", "Brands", "Login"];

  beforeEach(() => {
      cy.visit(URL)
  })

  describe('Should show the correct link in navbar', () => {
      navTitles.forEach((title) => {
          it(`Should show ${title} in nav`, () => {
              cy.get('.nav-menu').contains(title).should('exist')
          })
      })
  });

  const landingPageSections = ["Best Selling Products", "Best Reviewed Products"];

  describe('Should show products in landing page sections',() => {
      landingPageSections.forEach((section) => {
          it(`Should show products in ${section}`, () => {

              // validate the structure of each product card
              cy.contains('.section-title', section)
                  .parent('.container')
                  .get('.product-row-wrapper')
                  .find('.product-row')
                  .each(($card) => {
                      cy.wrap($card).within(() => {
                          cy.get('.product-image').should('exist');
                          cy.get('.product-title').should('not.be.empty');
                          cy.get('.product-description').should('exist');
                          cy.get('.product-price').should('exist');
                          cy.get('.product-actions .btn-primary').should('contain', 'Add to Cart');
                          cy.get('.product-actions .btn-secondary').should('contain', 'View');
                      });
                  });


          })
      })
  })

  describe('Should be able to login and place an order', () => {
      it('Get to checkout and place an order', () => {

          cy.contains('Login').click();
          cy.get('input[placeholder=Email]').type("user@nordic.com");
          cy.get('input[placeholder=Password]').type("user123");
          cy.get('button[type="submit"]').click();

          cy.contains('Login successful!').should('exist');
          cy.contains('Logout').should('exist');

          cy.contains('Add to Cart')
              .first()
              // button is behind navbar when logged in, therefore force
              .click({force: true});


          cy.get('.cart-icon')
              .click();

          cy.contains('Shipping Address')
              .click();

          cy.get('input[placeholder="Street name"]')
              .type('Street name');

          cy.get('input[placeholder="Street number"]')
              .type('42');

          cy.get('input[placeholder="ZIP Code"]')
              .type('4242');

          cy.get('input[placeholder="City"]')
              .type('City name')

          cy.intercept('POST', '/api/postgresql/stripe/checkout').as('checkout');

          cy.contains('Checkout').click();

          cy.wait('@checkout').then(({response}) => {
              expect(response.statusCode).to.eq(200);

              const {url: stripeUrl, orderId, sessionId} = response.body;

              cy.request({
                  method: 'POST',
                  url: '/api/postgresql/stripe/webhook',
                  body: {
                      type: 'checkout.session.completed',
                      data: {
                          object: {
                              id: sessionId
                          }
                      }
                  },
                  headers: {
                      'Stripe-Signature': 'test_signature'
                  }
              }).then((res) => {
                  expect(res.status).to.eq(200);
              });

              cy.visit(URL + "/orders");
              const shownOrderId = "#" + orderId.slice(0,8);

              cy.contains(shownOrderId)
                  .parent()
                  .find(".order-status")
                  .should("have.text", "confirmed");

          });
      })
  })

