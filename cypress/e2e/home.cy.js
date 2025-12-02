// E2E tests for the Nordic Electronics SPA using Cypress

const URL = 'http://localhost:8080/index.html';

describe('Nordic Electronics SPA E2E Testing', () => {
  it('show the landing page and that the header is loaded', () => {
    cy.visit(URL)
    cy.contains('Welcome to Nordic Electronics').should('be.visible')
    cy.get('button.cta-button').contains('Shop Now').should('be.visible')
  });

  it("Verify that navbar has home, products, categories, brands and login button", () => {
    cy.visit(URL)
    cy.get('.nav-menu').within(() => {
      cy.contains('Home').should('be.visible')
      cy.contains('Products').should('be.visible')
      cy.contains('Categories').should('be.visible')
      cy.contains('Brands').should('be.visible')
      cy.contains('Login').should('be.visible')
    });
  });

  it("searches for 'Laptop' using the search input", () => {
    cy.visit(URL)
    cy.get('#search-input')
      .should('be.visible')
      .type('laptop')

    cy.get('#search-button').should('be.visible').click()

    cy.get('#products-grid').should('be.visible')
    cy.get('#products-grid .product-card').should('have.length.gte', 3)

    // Verify each visible product title contains the word 'laptop' (case-insensitive)
    cy.get('#products-grid .product-title').each($el => {
      cy.wrap($el).invoke('text').then(text => {
        expect(text.toLowerCase()).to.include('laptop')
      })
    })
  });
});
